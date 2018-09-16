// =============================================================================================================================================================
// System: ZaraStar Accouts: Batch Definitions
// Module: AccountsBatchDefinitions.java
// Author: C.K.Harvey
// Copyright (c) 2003-08 Christopher Harvey. All Rights Reserved.
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
import java.sql.*;
 
public class AccountsBatchDefinitions extends HttpServlet
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsBatchDefinitions", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6022, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, int[] bytesOut)
                    throws Exception
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

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6022, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsBatchDefinitions", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6022, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsBatchDefinitions", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6022, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
  
    String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    create(con, stmt, rs, out, req, year, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6022, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), "");
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void create(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String year, String unm, String sid, String uty, String men, String den,
                      String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Accounts - Journal Batches</title>");
    
    scoutln(out, bytesOut, "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir)
                         + "general.css\">");

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
    scoutln(out, bytesOut, "var desc=sanitise(document.forms[0].desc.value);");
    scoutln(out, bytesOut, "var ob;if(document.forms[0].ob.checked)ob='Y';else ob='N';");
    scoutln(out, bytesOut, "var type;if(document.forms[0].type[0].checked)type='D';else if(document.forms[0].type[1].checked)type='C';else type='N';");

    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/AccountsBatchDefinitionUpdate?unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                         + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
                         + "') + \"&p1=\" + code + \"&p2=\" + date + \"&p3=\" + desc + \"&p4=\" + ob + \"&p5=\" + type + \"&p6=\" + newOrEdit + \"&p7="
                         + year + "&dnm=\" " + "+ escape('" + dnm + "');");
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
    
    // delete record from DB
    scoutln(out, bytesOut, "var req3;");    
    scoutln(out, bytesOut, "function initRequest3(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function del(code){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/AccountsEmptyBatchDelete?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&p2=" + year
                         + "&bnm=\" + escape('" + bnm + "') + \"&p1=\" + code + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\",url,true);");
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

    scoutln(out, bytesOut, "function edit(code)");
    scoutln(out, bytesOut, "{var url = \"http://" + men + "/central/servlet/AccountsBatchDefinitionEdit?unm=\" + escape('" + unm
                         + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty
                         + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&bnm=\" + escape('" + bnm
                           + "') + \"&p1=\" + code + \"&p2=" + year + "&dnm=\" + escape('" + dnm + "');");
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

    scoutln(out, bytesOut, "var date=req2.responseXML.getElementsByTagName(\"date\")[0].childNodes[0].nodeValue;document.forms[0].date.value=date;");

    scoutln(out, bytesOut, "var desc=req2.responseXML.getElementsByTagName(\"desc\")[0].childNodes[0].nodeValue;if(desc=='.')desc='';document.forms[0].desc.value=desc;");

    scoutln(out, bytesOut, "var ob=req2.responseXML.getElementsByTagName(\"ob\")[0].childNodes[0].nodeValue;if(ob=='Y')document.forms[0].ob.checked=true;else document.forms[0].ob.checked=false;");
    
    scoutln(out, bytesOut, "var type=req2.responseXML.getElementsByTagName(\"type\")[0].childNodes[0].nodeValue;if(type=='D')document.forms[0].type[0].checked=true;else if(type=='C')document.forms[0].type[1].checked=true;else document.forms[0].type[2].checked=true;");

    scoutln(out, bytesOut, "var dlm=req2.responseXML.getElementsByTagName(\"dlm\")[0].childNodes[0].nodeValue;document.forms[0].dlm.value=dlm;");

    scoutln(out, bytesOut, "var messageElement = document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Batch</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('code');");
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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/AccountsBatchDefinitions?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                         + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\");}");
    
    
    scoutln(out, bytesOut, "function entries(code,ob){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/AccountsBatchEntries?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men
                         + "&den=" + den + "&dnm=" + dnm + "&p1=\"+escape(code)+\"&p2=\"+ob+\"&bnm=" + bnm + "\";}");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6022", "", "AccountsBatchDefinitions", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out,  false, false, "", "", "", "", "", "","Accounts - Journal Batches: " + year, "6022", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=8><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");
    scoutln(out, bytesOut, "<td><p>Batch Code &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Description &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Type &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Opening Balances &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Last Modified &nbsp;</td><td></td></tr>");
        
    list(con, stmt, rs, out, year, unm, dnm, localDefnsDir, defnsDir, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=9><hr></td></tr>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><span id=\"code\"><input type=text name=code size=20 maxlength=20></span></td>");    
    scoutln(out, bytesOut, "<td><input type=text name=date size=10 maxlength=10></td>");
    scoutln(out, bytesOut, "<td><input type=text name=desc size=45 maxlength=80></td>");

    scoutln(out, bytesOut, "<td nowrap width=90%><p><input type=radio name=type>&nbsp;&nbsp;Debtor");
    scoutln(out, bytesOut, "<br><input type=radio name=type>&nbsp;&nbsp;Creditor");
    scoutln(out, bytesOut, "<br><input type=radio name=type>&nbsp;&nbsp;General</td>");

    scoutln(out, bytesOut, "<td colspan=2 width=90%><p><input type=checkbox name=ob>&nbsp;&nbsp;Opening Balances</td>");

    scoutln(out, bytesOut, "<tr><td><span id=\"dlm\"><input type=hidden name=dlm></span></td>"); /////////////////
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Batch</a></span></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection ofsaCon, Statement ofsaStmt, ResultSet ofsaRs, PrintWriter out, String year, String unm, String dnm, String localDefnsDir,
                    String defnsDir, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2   = null;
            
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);
      
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM joubatch ORDER BY Code"); 

      String code, date, desc, signOn, dlm, ob, openingBalances, type, cssFormat;
      boolean line1=true;

      while(rs.next())
      {
        code            = rs.getString(1);
        date            = rs.getString(2);
        desc            = rs.getString(3);
        signOn          = rs.getString(4);
        dlm             = rs.getString(5);
        ob = rs.getString(6);
        type            = rs.getString(7);
             
        if(line1) { cssFormat = "line1"; line1 = false; } else { cssFormat = "line2"; line1 = true; }
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
    
        scoutln(out, bytesOut, "<td>" + code + "</td>");
        scoutln(out, bytesOut, "<td>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
        scoutln(out, bytesOut, "<td>" + desc + "</td>");

        if(type.equals("D"))
          type = "Debtors";
        else
        if(type.equals("C"))
          type = "Creditors";
        else type = "General";
        scoutln(out, bytesOut, "<td>" + type + "</td>");

        if(ob.equals("Y"))
          openingBalances = "Yes";
        else openingBalances = "No";
        scoutln(out, bytesOut, "<td>" + openingBalances + "</td>");

        int x=0;
        while(x < dlm.length() && dlm.charAt(x) != ' ') // just-in-case
          ++x;
        scoutln(out, bytesOut, "<td>" + signOn + " on " + dlm.substring(0, x) +  "</td>");

        if(serverUtils.passLockCheck(ofsaCon, ofsaStmt, ofsaRs, "journalbatch", date, unm))
          scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + code + "')\">Edit</a></td>");
        else scoutln(out, bytesOut, "<td><p>Locked</td>");
          
        if(batchEmpty(con, stmt2, rs2, code))
          scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:del('" + code + "')\">Delete</a></td>");
        else scoutln(out, bytesOut, "<td></td>");
          
        scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:entries('" + code + "','" + ob + "')\">Entries</a></td></tr>");
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

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean batchEmpty(Connection con, Statement stmt, ResultSet rs, String batchCode) throws Exception
  {
    int rowcount = 0;
    
    try
    {
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT COUNT(*) as rowcount FROM joubatchl WHERE Code = '" + batchCode + "'"); 

      if(rs.next())
        rowcount = rs.getInt(1);
      
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }
    catch(Exception e)
    {
      System.out.println(e);
      if(rs   != null) rs.close();                                 
      if(stmt != null) stmt.close();
    }    
    
    if(rowcount == 0)
      return true;
    
    return false;
  }  
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
