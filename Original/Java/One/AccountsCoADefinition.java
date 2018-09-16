// =======================================================================================================================================================================================================
// System: ZaraStar Accouts: CoA Definition
// Module: AccountsCoADefinition.java
// Author: C.K.Harvey
// Copyright (c) 2003-07 Christopher Harvey. All Rights Reserved.
//
// This file is part of ZaraStar.
// ZaraStar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
// ZaraStar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
// You should have received a copy of the GNU General Public License along with ZaraStar. If not, see <http://www.gnu.org/licenses/>.
// =======================================================================================================================================================================================================

package org.zarastar.zarastar;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
 
public class AccountsCoADefinition extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  PageFrameUtils pageFrameUtils = new PageFrameUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  AccountsUtils accountsUtils = new AccountsUtils();
  
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsCoADefinition", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6016, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den,
                    String dnm, String bnm, int[] bytesOut) throws Exception
  {
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir       = directoryUtils.getUserDir('W', dnm, unm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6016, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsCoADefinition", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6016, bytesOut[0], 0, "ACC:");
    if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsCoADefinition", imagesDir, localDefnsDir,
                         defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6016, bytesOut[0], 0, "SID:");
    if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    create(con, stmt, rs, out, req, year, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6016, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String year, String unm, String sid, String uty,
                      String men, String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                      throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Accounts - Account Definition</title>");
    
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
    
    scoutln(out, bytesOut, "var accCode=sanitise(document.forms[0].accCode.value);");

    scoutln(out, bytesOut, "var category=sanitise(document.forms[0].category.value);");

    scoutln(out, bytesOut, "var drcr;if(document.forms[0].drcr[0].checked)drcr='D';else drcr='C';");

    scoutln(out, bytesOut, "var type;if(document.forms[0].type[0].checked)type='T';else type='B';");
    
    scoutln(out, bytesOut, "var active;if(document.forms[0].active.checked)active='Y';else active='N';");

    scoutln(out, bytesOut, "var desc=sanitise(document.forms[0].desc.value);");

    scoutln(out, bytesOut, "var currency=sanitise(document.forms[0].currency.value);");

    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/AccountsCoADefinitionUpdate?unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                         + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
                         + "') + \"&p1=\" + accCode + \"&p2=\" + category + \"&p3=\" + drcr + \"&p4=\" + desc + "
                         + "\"&p5=\" + type + \"&p7=\" + currency + \"&p8=\" + active + \"&p12=\" + newOrEdit + \"&p9="  + year + "&dnm=\" "
                         + "+ escape('" + dnm + "');");
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

    scoutln(out, bytesOut, "function edit(accCode)");
    scoutln(out, bytesOut, "{var url = \"http://" + men + "/central/servlet/AccountsCoADefinitionEdit?unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                         + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
                           + "') + \"&p1=\" + accCode + \"&p2=" + year + "&dnm=\" + escape('" + dnm + "');");
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
    scoutln(out, bytesOut, "var accCode=req2.responseXML.getElementsByTagName(\"accCode\")[0].childNodes[0].nodeValue;document.forms[0].accCode.value=accCode;");

    scoutln(out, bytesOut, "var category=req2.responseXML.getElementsByTagName(\"category\")[0].childNodes[0].nodeValue;document.forms[0].category.value=category;");

    scoutln(out, bytesOut, "var drcr=req2.responseXML.getElementsByTagName(\"drcr\")[0].childNodes[0].nodeValue;if(drcr=='D')document.forms[0].drcr[0].checked=true;else document.forms[0].drcr[1].checked=true;");

    scoutln(out, bytesOut, "var type=req2.responseXML.getElementsByTagName(\"type\")[0].childNodes[0].nodeValue;if(type=='T')document.forms[0].type[0].checked=true;else document.forms[0].type[1].checked=true;");

    scoutln(out, bytesOut, "var desc=req2.responseXML.getElementsByTagName(\"desc\")[0].childNodes[0].nodeValue;if(desc=='.')desc='';document.forms[0].desc.value=desc;");

    scoutln(out, bytesOut, "var active=req2.responseXML.getElementsByTagName(\"active\")[0].childNodes[0].nodeValue;if(active=='Y')document.forms[0].active.checked=true;else document.forms[0].active.checked=false;");
    
    scoutln(out, bytesOut, "var currency=req2.responseXML.getElementsByTagName(\"currency\")[0].childNodes[0].nodeValue;document.forms[0].currency.value=currency;");

    scoutln(out, bytesOut, "var dlm=req2.responseXML.getElementsByTagName(\"dlm\")[0].childNodes[0].nodeValue;document.forms[0].dlm.value=dlm;");

    scoutln(out, bytesOut, "var messageElement = document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Account</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('accCode');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=accCode value=\"'+accCode+'\">'+accCode;");
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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/AccountsCoADefinition?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\");}");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6016", "", "AccountsCoADefinition", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out, false, false, "", "", "", "", "", "", "Accounts - Account Definition: " + year, "6016", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=8><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td nowrap><p>Account Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Description &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Dr/Cr &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Type &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Currency &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Category &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Active &nbsp;</td>");
    scoutln(out, bytesOut, "<td nowrap><p>Last Modified &nbsp;</td><td></td></tr>");
        
    list(out, year, dnm, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=9><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><span id=\"accCode\"><input type=text name=accCode size=20 maxlength=20></span></td>");    
    scoutln(out, bytesOut, "<td><input type=text name=desc size=25 maxlength=60></td>");

    scoutln(out, bytesOut, "<td nowrap width=90%><p><input type=radio name=drcr>&nbsp;&nbsp;Debit");
    scoutln(out, bytesOut, "<br><input type=radio name=drcr>&nbsp;&nbsp;Credit</td>");

    scoutln(out, bytesOut, "<td nowrap><p><input type=radio name=type>&nbsp;&nbsp;Trading");
    scoutln(out, bytesOut, "<br><input type=radio name=type>&nbsp;&nbsp;Balance Sheet</td>");

    scoutln(out, bytesOut, "<td>" + accountsUtils.getCurrencyNamesDDL(con, stmt, rs, "currency", dnm, localDefnsDir, defnsDir) + "</td>");

    scoutln(out, bytesOut, "<td>" + accountsUtils.getCategoryNamesDDL("category") + "</td>");

    scoutln(out, bytesOut, "<td nowrap width=90%><p><input type=checkbox name=active>&nbsp;&nbsp;Active</td>");

    scoutln(out, bytesOut, "<tr><td><span id=\"dlm\"><input type=hidden name=dlm></span></td>"); // TODO
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Account</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------
  private void list(PrintWriter out, String year, String dnm, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String userName = directoryUtils.getMySQLUserName();
      String passWord = directoryUtils.getMySQLPassWord();
    
      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + userName + "&password=" + passWord);
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT AccCode, Description, DrCr, Type, DateLastModified, Currency, Active, Category, SignOn FROM acctdefn ORDER BY AccCode"); 

      String accCode, category, drcr, desc, type, dlm, currency, active, signOn, cssFormat;
      boolean line1=true;

      while(rs.next())                  
      {
        accCode     = rs.getString(1);
        desc        = rs.getString(2);
        drcr        = rs.getString(3);
        type        = rs.getString(4);
        dlm         = rs.getString(5);
        currency    = rs.getString(6);
        active      = rs.getString(7);
        category    = rs.getString(8);
        signOn      = rs.getString(9);
      
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
    
        scoutln(out, bytesOut, "<td>" + accCode + "</td>");
        scoutln(out, bytesOut, "<td nowrap>" + desc + "</td>");

        if(drcr.equals("D"))
          drcr = "Debit";
        else drcr = "Credit";
        scoutln(out, bytesOut, "<td>" + drcr + "</td>");

        if(type.equals("T"))
          type = "Trading";
        else type = "Balance Sheet";
        scoutln(out, bytesOut, "<td>" + type + "</td>");
        
        scoutln(out, bytesOut, "<td>" + currency + "</td>");

        scoutln(out, bytesOut, "<td nowrap>" + category + "</td>");
        
        if(active.equals("Y")       || active.equals("A"))/////////////
          active = "Yes";
        else active = "";
        scoutln(out, bytesOut, "<td>" + active + "</td>");

        int x=0;
        while(x < dlm.length() && dlm.charAt(x) != ' ') // just-in-case
          ++x;
        scoutln(out, bytesOut, "<td nowrap>" + signOn + " on " + dlm.substring(0, x) +  "</td>");

        scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + accCode + "')\">Edit</a></td></tr>");
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
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
