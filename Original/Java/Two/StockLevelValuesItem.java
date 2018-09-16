// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock Levels & Values for an item
// Module: StockLevelValuesItem.java
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

public class StockLevelValuesItem extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  DocumentUtils documentUtils = new DocumentUtils();

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

      if(p1 == null) p1 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockLevelValuesItem", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3069, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3069, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3069", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3069, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3069", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3069, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
  
    create(con, stmt, rs, out, req, p1.toUpperCase(), unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3069, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String itemCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                      String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Values by Item Code</title>");
    
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
    scoutln(out, bytesOut, "var code=sanitise(document.forms[0].code.value);");
    scoutln(out, bytesOut, "var date=sanitise(document.forms[0].date.value);");
    scoutln(out, bytesOut, "var cost=sanitise(document.forms[0].cost.value);");
    scoutln(out, bytesOut, "var level=sanitise(document.forms[0].level.value);");

    scoutln(out, bytesOut, "var status='L';if(document.forms[0].status.checked)status='C';");
    
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/StockLevelValuesUpdate?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + code + \"&p2=" + itemCode
                         + "&p3=\" + cost + \"&p4=\" + date + \"&p5=\" + level + \"&p6=\" + status + \"&p7=\" + newOrEdit + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest4(url);");
    scoutln(out, bytesOut, "req4.onreadystatechange = processRequest4;");
    scoutln(out, bytesOut, "req4.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req4.send(null);}");

    scoutln(out, bytesOut, "function processRequest4(){");
    scoutln(out, bytesOut, "if(req4.readyState==4){");
    scoutln(out, bytesOut, "if(req4.status==200){");
    scoutln(out, bytesOut, "var res=req4.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0)");
    scoutln(out, bytesOut, "{if(res=='.')");
    scoutln(out, bytesOut, "refetchPage();else{var messageElement=document.getElementById('msg');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<span id=\"textErrorLarge\">'+res+'</span>';");
    scoutln(out, bytesOut, "}}}}}");
      
    // fetch rec for edit
    scoutln(out, bytesOut, "var req2;");    
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function edit(code){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/StockLevelValuesEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + code + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "req2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(req2.readyState==4){");
    scoutln(out, bytesOut, "if(req2.status == 200){");
    scoutln(out, bytesOut, "var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var code=req2.responseXML.getElementsByTagName(\"code\")[0].childNodes[0].nodeValue;document.forms[0].code.value=code;");
    scoutln(out, bytesOut, "var cost=req2.responseXML.getElementsByTagName(\"cost\")[0].childNodes[0].nodeValue;document.forms[0].cost.value=cost;");
    scoutln(out, bytesOut, "var date=req2.responseXML.getElementsByTagName(\"date\")[0].childNodes[0].nodeValue;document.forms[0].date.value=date;");
    scoutln(out, bytesOut, "var level=req2.responseXML.getElementsByTagName(\"level\")[0].childNodes[0].nodeValue;document.forms[0].level.value=level;");
    scoutln(out, bytesOut, "var status=req2.responseXML.getElementsByTagName(\"status\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var signon=req2.responseXML.getElementsByTagName(\"signon\")[0].childNodes[0].nodeValue;");

    scoutln(out, bytesOut, "var messageElement=document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Record</a>';");
    
    scoutln(out, bytesOut, "messageElement=document.getElementById('codebox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=code value=\"'+code+'\">'+code;");
    
    scoutln(out, bytesOut, "messageElement=document.getElementById('status');if(status=='C')");
    scoutln(out, bytesOut, "messageElement.innerHTML='<input type=checkbox name=status checked>';");
    scoutln(out, bytesOut, "else messageElement.innerHTML='<input type=checkbox name=status>';");
    
    scoutln(out, bytesOut, "messageElement=document.getElementById('signon');");
    scoutln(out, bytesOut, "messageElement.innerHTML=signon;");

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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/StockLevelValuesItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&bnm=" + bnm + "\");}");
    
    scoutln(out, bytesOut, "</script>");

    outputPageFrame(con, stmt, rs, out, req, "", "Stock Value for Item Code " + itemCode, unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);
 
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=7><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Cancelled</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=center><p>Cost &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap align=center><p>Level &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Last Modified By &nbsp;</td><td></td></tr>");
        
    list(con, stmt, rs, out, itemCode, unm, imagesDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=8><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><span id=\"status\"><input type=checkbox name=status></span></td>");
    scoutln(out, bytesOut, "<td><span id=\"codebox\"><input type=hidden name=code value=''></span></td>");
    
    scoutln(out, bytesOut, "<td><input type=text name=date size=10 maxlength=10></td>");

    scoutln(out, bytesOut, "<td><input type=text name=cost size=6 maxlength=20></td>");
    
    scoutln(out, bytesOut, "<td><input type=text name=level size=5 maxlength=20></td>");
    scoutln(out, bytesOut, "<td><span id=\"signon\"></span></td>");
    scoutln(out, bytesOut, "<td><input type=text name=remark size=20 maxlength=80></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Record</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String unm, String imagesDir, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT * FROM stockopen WHERE ItemCode = '" + itemCode + "' ORDER BY Date");

      String code, cost, date, level, status, signOn, dlm, cssFormat = "line1";

      while(rs.next())                  
      {
        code   = rs.getString(1);
        date   = rs.getString(3);
        level  = rs.getString(4);
        cost   = rs.getString(5);
        status = rs.getString(6);
        signOn = rs.getString(7);
        dlm    = rs.getString(8);
       
        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        
        if(status.equals("C"))
          scoutln(out, bytesOut, "<td align=center><p><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");        
        else scoutln(out, bytesOut, "<td></td>");
        
        scoutln(out, bytesOut, "<td><p>" + code + "</td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.formatNumeric(cost, '2') + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stripTrailingZeroesStr(level) + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + signOn + " on " + dlm.substring(0, (dlm.length() - 2)) + "</td>");

        if(serverUtils.passLockCheck(con, stmt, rs, "stockopen", date, unm))
          scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:edit('" + code + "')\">Edit</a></td></tr>");
        else scoutln(out, bytesOut, "<td></td></tr>");
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String bodyStr, String title, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                               String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "3069", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(3069) + "</td></tr></table>";

    subMenuText += buildSubMenuText(hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "StockLevelValuesItem", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
