// =======================================================================================================================================================================================================
// System: ZaraStar Product: Stock Adjustment records for an item
// Module: StockAdjustmentItem.java
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

public class StockAdjustmentItem extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "StockAdjustmentItem", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3011, bytesOut[0], 0, "ERR:" + p1);
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 3011, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "3011", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3011, bytesOut[0], 0, "ACC:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "3011", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 3011, bytesOut[0], 0, "SID:" + p1);
      if(con  != null) con.close();
      if(out != null) out.flush(); 
      return;
    }
  
    create(con, stmt, rs, out, req, p1.toUpperCase(), unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 3011, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush(); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String itemCode, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String imagesDir,
                      String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Stock Adjustment by Item Code</title>");
    
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
    scoutln(out, bytesOut, "{var code=sanitise(document.forms[0].code.value);");
    scoutln(out, bytesOut, "var date=sanitise(document.forms[0].date.value);");
    scoutln(out, bytesOut, "var storeFrom=sanitise(document.forms[0].storeFrom.value);");
    scoutln(out, bytesOut, "var storeTo=sanitise(document.forms[0].storeTo.value);");
    scoutln(out, bytesOut, "var locationFrom=sanitise(document.forms[0].locationFrom.value);");
    scoutln(out, bytesOut, "var locationTo=sanitise(document.forms[0].locationTo.value);");
    scoutln(out, bytesOut, "var qty=sanitise(document.forms[0].qty.value);");
    scoutln(out, bytesOut, "var soCode=sanitise(document.forms[0].soCode.value);");
    scoutln(out, bytesOut, "var poCode=sanitise(document.forms[0].poCode.value);");
    scoutln(out, bytesOut, "var remark=sanitise(document.forms[0].remark.value);");

    scoutln(out, bytesOut, "var status='L';if(document.forms[0].status.checked)status='C';");
    
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/StockAdjustmentUpdate?unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                         + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
                         + "') + \"&p1=\" + code + \"&p2=" + itemCode
                         + "&p3=\" + storeFrom + \"&p4=\" + storeTo + \"&p5=\" + date + \"&p6=\" + qty + \"&p7=\" + remark + \"&p8=\" + status + \"&p9=\" + soCode + \"&p10=\" + poCode + \"&p11=\" + locationFrom + \"&p12=\" + locationTo + \"&p13=\" + newOrEdit + \"&dnm=\" + escape('"
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
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/StockAdjustmentEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den
                         + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + code + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "req2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2()");
    scoutln(out, bytesOut, "{if(req2.readyState==4)");
    scoutln(out, bytesOut, "{if(req2.status == 200)");
    scoutln(out, bytesOut, "{var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length > 0)");
    scoutln(out, bytesOut, "{if(res=='.'){");
    scoutln(out, bytesOut, "var code=req2.responseXML.getElementsByTagName(\"code\")[0].childNodes[0].nodeValue;document.forms[0].code.value=code;");
    scoutln(out, bytesOut, "var storeFrom=req2.responseXML.getElementsByTagName(\"storeFrom\")[0].childNodes[0].nodeValue;document.forms[0].storeFrom.value=storeFrom;");
    scoutln(out, bytesOut, "var storeTo=req2.responseXML.getElementsByTagName(\"storeTo\")[0].childNodes[0].nodeValue;document.forms[0].storeTo.value=storeTo;");
    scoutln(out, bytesOut, "var locationFrom=req2.responseXML.getElementsByTagName(\"locationFrom\")[0].childNodes[0].nodeValue;document.forms[0].locationFrom.value=locationFrom;");
    scoutln(out, bytesOut, "var locationTo=req2.responseXML.getElementsByTagName(\"locationTo\")[0].childNodes[0].nodeValue;document.forms[0].locationTo.value=locationTo;");
    scoutln(out, bytesOut, "var date=req2.responseXML.getElementsByTagName(\"date\")[0].childNodes[0].nodeValue;document.forms[0].date.value=date;");
    scoutln(out, bytesOut, "var qty=req2.responseXML.getElementsByTagName(\"qty\")[0].childNodes[0].nodeValue;document.forms[0].qty.value=qty;");
    scoutln(out, bytesOut, "var soCode=req2.responseXML.getElementsByTagName(\"soCode\")[0].childNodes[0].nodeValue;document.forms[0].soCode.value=soCode;");
    scoutln(out, bytesOut, "var poCode=req2.responseXML.getElementsByTagName(\"poCode\")[0].childNodes[0].nodeValue;document.forms[0].poCode.value=poCode;");
    scoutln(out, bytesOut, "var remark=req2.responseXML.getElementsByTagName(\"remark\")[0].childNodes[0].nodeValue;document.forms[0].remark.value=remark;");
    scoutln(out, bytesOut, "var status=req2.responseXML.getElementsByTagName(\"status\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var signon=req2.responseXML.getElementsByTagName(\"signon\")[0].childNodes[0].nodeValue;");

    scoutln(out, bytesOut, "var messageElement=document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Stock Adjustment Record</a>';");
    
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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/StockAdjustmentItem?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + itemCode + "&bnm=" + bnm + "\");}");
    
    scoutln(out, bytesOut, "</script>");

    outputPageFrame(con, stmt, rs, out, req, "", "Stock Adjustment for Item Code " + itemCode, unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, bytesOut);
 
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=7><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td></td>"); // status
    scoutln(out, bytesOut, "<td nowrap><p>Adjustment Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Store From &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Location From &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Store To &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Location To &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Quantity &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>SOCode &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>POCode &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Created By &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Remark &nbsp;</td><td></td></tr>");
        
    list(out, itemCode, unm, dnm, imagesDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=9><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><span id=\"status\"><input type=checkbox name=status></span></td>");
    scoutln(out, bytesOut, "<td><span id=\"codebox\"><input type=hidden name=code value=''></span></td>");
    
    scoutln(out, bytesOut, "<td><input type=text name=date size=10 maxlength=10></td>");

    scoutln(out, bytesOut, "<td><span id=\"storeFrom\">");
    scoutln(out, bytesOut, documentUtils.getStoresDDL(con, stmt, rs, "storeFrom", "<option value=\"None\">None"));
    scoutln(out, bytesOut, "</span></td>");
    
    scoutln(out, bytesOut, "<td><input type=text name=locationFrom size=10 maxlength=20></td>");

    scoutln(out, bytesOut, "<td><span id=\"storeTo\">");
    scoutln(out, bytesOut, documentUtils.getStoresDDL(con, stmt, rs, "storeTo", "<option value=\"None\">None"));
    scoutln(out, bytesOut, "</span></td>");
    
    scoutln(out, bytesOut, "<td><input type=text name=locationTo size=10 maxlength=20></td>");

    scoutln(out, bytesOut, "<td><input type=text name=qty size=5 maxlength=20></td>");

    scoutln(out, bytesOut, "<td><input type=text name=soCode size=10 maxlength=20></td>");
    scoutln(out, bytesOut, "<td><input type=text name=poCode size=10 maxlength=20></td>");
    
    scoutln(out, bytesOut, "<td><span id=\"signon\"></span></td>");
    scoutln(out, bytesOut, "<td><input type=text name=remark size=20 maxlength=80></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Stock Adjustment Record</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir));
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(PrintWriter out, String itemCode, String unm, String dnm, String imagesDir, int[] bytesOut) throws Exception
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
  
      rs = stmt.executeQuery("SELECT * FROM stocka WHERE ItemCode = '" + itemCode + "' ORDER BY Date");

      String adjCode, storeFrom, storeTo, date, qty, remark, status, signOn, dlm, soCode, poCode, cssFormat = "line1", locationFrom, locationTo;

      while(rs.next())                  
      {
        adjCode   = rs.getString(1);
        date      = rs.getString(3);
        qty       = rs.getString(4);
        signOn    = rs.getString(5);
        remark    = rs.getString(6);
        storeFrom = rs.getString(7);
        storeTo   = rs.getString(8);
        dlm       = rs.getString(9);
        status    = rs.getString(10);
        soCode    = rs.getString(11);
        poCode    = rs.getString(12);
        locationFrom = rs.getString(13);
        locationTo   = rs.getString(13);
        
        if(cssFormat.equals("line2")) cssFormat = "line1"; else cssFormat = "line2";
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
        
        if(status.equals("C"))
          scoutln(out, bytesOut, "<td align=center><img src=\"" + imagesDir + "z0212.gif\" border=0></td>");        
        else scoutln(out, bytesOut, "<td></td>");
        
        scoutln(out, bytesOut, "<td><p>" + adjCode + "</td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
        scoutln(out, bytesOut, "<td><p>" + storeFrom + "</td>");
        scoutln(out, bytesOut, "<td><p>" + locationFrom + "</td>");
        scoutln(out, bytesOut, "<td><p>" + storeTo + "</td>");
        scoutln(out, bytesOut, "<td><p>" + locationTo + "</td>");
        scoutln(out, bytesOut, "<td><p>" + generalUtils.stripTrailingZeroesStr(qty) + "</td>");
        scoutln(out, bytesOut, "<td><p>" + soCode + "</td>");
        scoutln(out, bytesOut, "<td><p>" + poCode + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + signOn + " at " + dlm.substring(0, (dlm.length() - 2)) + "</td>");
        scoutln(out, bytesOut, "<td nowrap><p>" + remark + "</td>");

        if(serverUtils.passLockCheck(con, stmt, rs, "stocka", date, unm))
          scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + adjCode + "')\">Edit</a></td></tr>");
        else scoutln(out, bytesOut, "<td></td></tr>");
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

  // -------------------------------------------------------------------------------------------------------------------------------
  private void outputPageFrame(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String bodyStr, String title, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                              String otherSetup, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    int[] hmenuCount = new int[1];

    String subMenuText = dashboardUtils.buildSubMenuText(con, stmt, rs, req, "3011", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount);

    subMenuText += "<table id='title' width=100%><tr><td colspan=2 nowrap><p>" + title + directoryUtils.buildHelp(3011) + "</td></tr></table>";

    subMenuText += buildSubMenuText(unm, sid, uty, men, den, dnm, bnm, hmenuCount);

    scoutln(out, bytesOut, authenticationUtils.buildScreen(con, stmt, rs, req, true, unm, sid, uty, men, den, dnm, bnm, "StockAdjustmentItem", subMenuText, hmenuCount[0], bodyStr, otherSetup, localDefnsDir, defnsDir));

    scoutln(out, bytesOut, "<table border=0><tr><td valign=top><div id='second' style=\"{margin-top:0px;}\"></div></td>");
    scoutln(out, bytesOut, "<td valign=top><div id='third' style=\"{margin-top:0px;margin-left:0px;}\"></div></td></tr></table>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String buildSubMenuText(String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] hmenuCount) throws Exception
  {
    String s = "<div id='submenu'>";

    ++hmenuCount[0];
    
    s += "</div>";
    
    --hmenuCount[0];
    
    return s;
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
