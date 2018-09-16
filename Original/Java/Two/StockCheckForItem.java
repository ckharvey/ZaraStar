// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock Check records for an item
// Module: StockCheckForItem.java
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

public class StockCheckForItem extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  DashboardUtils dashboardUtils = new DashboardUtils();
  DocumentUtils documentUtils = new DocumentUtils();
  Inventory inventory = new Inventory();

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="";

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
      p1  = req.getParameter("p1"); // itemCode
      p2  = req.getParameter("p2"); // mfr
      p3  = req.getParameter("p3"); // mfrCode

      if(p1 == null) p1 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockCheckForItem", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3018, bytesOut[0], 0, "ERR:" + p1 + ":" + p2);
      if(out != null) out.flush(); 
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3018, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3018", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3018, bytesOut[0], 0, "ACC:" + p1 + ":" + p2);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3018", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3018, bytesOut[0], 0, "SID:" + p1 + ":" + p2);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(p1.length() == 0)
    {
      String itemCodeStr = inventory.getItemCodeGivenMfrAndMfrCode(con, stmt, rs, p2, p3);
      if(itemCodeStr.length() == 0)
      {
        scoutln(out, bytesOut, "<html><head><title>Stock Check by Manufacturer Code</title>");

        scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

        outputPageFrame(con, stmt, rs, out, req, "", "Stock Check for Manufacturer Code: " + p3, unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);
 
        scoutln(out, bytesOut, "<br><br><p>Manufacturer: " + p2 + ", and Manufacturer Code: " + p3 + " Not Found<br><br>");
        scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
        return;
      }

      p1 = itemCodeStr;
    }

    create(con, stmt, rs, out, req, p1.toUpperCase(), unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3018, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1 + ":" + p2);
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String itemCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                      String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Check by Item Code</title>");
    
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
    scoutln(out, bytesOut, "var store=sanitise(document.forms[0].store.value);");
    scoutln(out, bytesOut, "var location=sanitise(document.forms[0].location.value);");
    scoutln(out, bytesOut, "var type=document.forms[0].type.value;");
    scoutln(out, bytesOut, "var level=sanitise(document.forms[0].level.value);");
    scoutln(out, bytesOut, "var remark=sanitise(document.forms[0].remark.value);");

    scoutln(out, bytesOut, "var status='L';if(document.forms[0].status.checked)status='C';");
    scoutln(out, bytesOut, "var reconciled='N';if(document.forms[0].reconciled.checked)reconciled='Y';");
    
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/StockCheckEditUpdate?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + code + \"&p2=" + itemCode + "&p3=\" + store + \"&p4=\" + date + \"&p5=\" + level + \"&p6=\" + remark + \"&p7=\" + status + \"&p8=\" + reconciled "
                         + "+ \"&p9=\" + type + \"&p10=\" + location + \"&p11=\" + newOrEdit + \"&dnm=\" + escape('" + dnm + "');");
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
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/StockCheckEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + code + \"&dnm=\" + escape('" + dnm + "');");
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
    scoutln(out, bytesOut, "var store=req2.responseXML.getElementsByTagName(\"store\")[0].childNodes[0].nodeValue;document.forms[0].store.value=store;");
    scoutln(out, bytesOut, "var location=req2.responseXML.getElementsByTagName(\"location\")[0].childNodes[0].nodeValue;document.forms[0].location.value=location;");
    scoutln(out, bytesOut, "var type=req2.responseXML.getElementsByTagName(\"type\")[0].childNodes[0].nodeValue;document.forms[0].type.value=type;");
    scoutln(out, bytesOut, "var date=req2.responseXML.getElementsByTagName(\"date\")[0].childNodes[0].nodeValue;document.forms[0].date.value=date;");
    scoutln(out, bytesOut, "var level=req2.responseXML.getElementsByTagName(\"level\")[0].childNodes[0].nodeValue;document.forms[0].level.value=level;");
    scoutln(out, bytesOut, "var remark=req2.responseXML.getElementsByTagName(\"remark\")[0].childNodes[0].nodeValue;document.forms[0].remark.value=remark;");
    scoutln(out, bytesOut, "var status=req2.responseXML.getElementsByTagName(\"status\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var reconciled=req2.responseXML.getElementsByTagName(\"reconciled\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var signon=req2.responseXML.getElementsByTagName(\"signon\")[0].childNodes[0].nodeValue;");

    scoutln(out, bytesOut, "var messageElement=document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Stock Check Record</a>';");
    
    scoutln(out, bytesOut, "messageElement=document.getElementById('codebox');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=code value=\"'+code+'\">'+code;");
    
    scoutln(out, bytesOut, "messageElement=document.getElementById('status');if(status=='C')");
    scoutln(out, bytesOut, "messageElement.innerHTML='<input type=checkbox name=status checked>';");
    scoutln(out, bytesOut, "else messageElement.innerHTML='<input type=checkbox name=status>';");
    
    scoutln(out, bytesOut, "messageElement=document.getElementById('reconciled');if(reconciled=='Y')");
    scoutln(out, bytesOut, "messageElement.innerHTML='<input type=checkbox name=reconciled checked>';");
    scoutln(out, bytesOut, "else messageElement.innerHTML='<input type=checkbox name=reconciled>';");
    
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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/StockCheckForItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&bnm=" + bnm + "\");}");
    
    scoutln(out, bytesOut, "</script>");

    outputPageFrame(con, stmt, rs, out, req, "", "Stock Check for Item Code: " + itemCode, unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);
 
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=8><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Cancelled</td>");
    scoutln(out, bytesOut, "<td><p>Reconciled</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Check Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Store &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Location &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Level &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Type &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Last Modified By &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Remark &nbsp;</td><td></td></tr>");
        
    list(con, stmt, rs, out, itemCode, unm, imagesDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=10><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><span id=\"status\"><input type=checkbox name=status></span></td>");
    scoutln(out, bytesOut, "<td><span id=\"reconciled\"><input type=checkbox name=reconciled></span></td>");
    scoutln(out, bytesOut, "<td><span id=\"codebox\"><input type=hidden name=code value=''></span></td>");
    
    scoutln(out, bytesOut, "<td><input type=text name=date size=10 maxlength=10></td>");

    scoutln(out, bytesOut, "<td><span id=\"store\">" + documentUtils.getStoresDDL(con, stmt, rs, "store") + "</span></td>");
    
    scoutln(out, bytesOut, "<td><input type=text name=location size=10 maxlength=20></td>");

    scoutln(out, bytesOut, "<td><input type=text name=level size=5 maxlength=20></td>");

    scoutln(out, bytesOut, "<td><span id='type'>");
    getTypeDDL(out, bytesOut);
    scoutln(out, bytesOut, "</span></td>");
    
    scoutln(out, bytesOut, "<td><span id=\"signon\"></span></td>");
    scoutln(out, bytesOut, "<td><input type=text name=remark size=20 maxlength=80></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Stock Check Record</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String itemCode, String unm, String imagesDir, int[] bytesOut) throws Exception
  {
    try
    {
      stmt = con.createStatement();
  
      rs = stmt.executeQuery("SELECT * FROM stockc WHERE ItemCode = '" + itemCode + "' AND Level != '999999' ORDER BY Date");

      String checkCode, store, date, level, remark, status, signOn, dlm, reconciled, type, cssFormat = "line1", location;

      while(rs.next())                  
      {
        checkCode  = rs.getString(1);
        store      = rs.getString(3);
        date       = rs.getString(4);
        level      = rs.getString(5);
        remark     = rs.getString(6);
        status     = rs.getString(7);
        signOn     = rs.getString(8);
        dlm        = rs.getString(9);
        location   = rs.getString(10);
        reconciled = rs.getString(12);
        type       = rs.getString(13);
        
        if(reconciled == null) reconciled = "N";
        if(type       == null) type       = "S";
        
        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        
        if(status.equals("C"))
          scoutln(out, bytesOut, "<td align=center><p><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");        
        else scoutln(out, bytesOut, "<td></td>");
        
        if(reconciled.equals("Y"))
          scoutln(out, bytesOut, "<td align=center><p>Yes</td>");        
        else scoutln(out, bytesOut, "<td align=center><p>No</td>");
        
        scoutln(out, bytesOut, "<td><p>" + checkCode + "</td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
        scoutln(out, bytesOut, "<td><p>" + store + "</td>");
        scoutln(out, bytesOut, "<td><p>" + location + "</td>");
        scoutln(out, bytesOut, "<td align=center><p>" + generalUtils.stripTrailingZeroesStr(level) + "</td>");

        if(type.equals("S"))
          scoutln(out, bytesOut, "<td align=center><p>Stock</td>");        
        else scoutln(out, bytesOut, "<td align=center><p>WIP</td>");
        
        scoutln(out, bytesOut, "<td nowrap><p>" + signOn + " at " + dlm.substring(0, (dlm.length() - 2)) + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + remark + "</td>");

        if(serverUtils.passLockCheck(con, stmt, rs, "stockc", date, unm))
          scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:edit('" + checkCode + "')\">Edit</a></td></tr>");
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String bodyStr, String title, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                               String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "3018", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(3018) + "</td></tr></table>";

    subMenuText += buildSubMenuText(unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "StockCheckForItem", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];

    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void getTypeDDL(PrintWriter out, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name='type'>");

    try
    {
      scoutln(out, bytesOut, "<option value='S'>Shelf Stock");
      scoutln(out, bytesOut, "<option value='W'>WIP");
    }
    catch(Exception e)
    {
      System.out.println("3018: " + e);
    }
    
    scoutln(out, bytesOut, "</select>");
  }

}
