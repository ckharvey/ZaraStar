// =======================================================================================================================================================================================================
// System: ZaraStar Accouts: Batch Entries
// Module: AccountsBatchEntries.java
// Author: C.K.Harvey
// Copyright (c) 2003-09 Christopher Harvey. All Rights Reserved.
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
 
public class AccountsBatchEntries extends HttpServlet
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

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="";

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
      p2  = req.getParameter("p2");

      if(p2 == null) p2 = "";

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "AccountsBatchEntries", bytesOut);
      serverUtils.totalBytes(req, unm, dnm, 6023, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String workingDir    = directoryUtils.getUserDir('W', dnm, unm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 6022, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "AccountsBatchEntries", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6023, bytesOut[0], 0, "ACC:" + p1);
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "AccountsBatchEntries", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 6023, bytesOut[0], 0, "SID:" + p1);
      if(out != null) out.flush();
      return;
    }
  
    String year = generalUtils.getFromDefnFile(unm, "accounts.dfn", workingDir, "");

    boolean notOpeningBalances;

    if(p2.length() == 0)
      notOpeningBalances = areNotOpeningBalances(p1, year, dnm);
    else
    if(p2.equals("N"))
      notOpeningBalances = true;
    else notOpeningBalances = false;

    editBatch(con, stmt, rs, out, req, year, p1, p2, notOpeningBalances, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 6023, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void editBatch(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String year, String batchCode, String p2, boolean notOpeningBalances, String unm, String sid, String uty, String men, String den, String dnm,
                         String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Accounts - Journal Batches: " + batchCode + "</title>");
    
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
    scoutln(out, bytesOut, "var transactionDate=sanitise(document.forms[0].transactionDate.value);");
    scoutln(out, bytesOut, "var docCode=sanitise(document.forms[0].docCode.value);");
    scoutln(out, bytesOut, "var accCode=sanitise(document.forms[0].accCode.value);");
    scoutln(out, bytesOut, "var amount=sanitise(document.forms[0].amount.value);");
    scoutln(out, bytesOut, "var remark=sanitise(document.forms[0].remark.value);");
    scoutln(out, bytesOut, "var journal='XX';");
    scoutln(out, bytesOut, "var currency=sanitise(document.forms[0].currency.value);");
    scoutln(out, bytesOut, "var baseAmount=sanitise(document.forms[0].baseAmount.value);");
    scoutln(out, bytesOut, "var drCr;if(document.forms[0].drCr[0].checked)drCr='D';else drCr='C';");

    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/AccountsBatchEntryUpdate?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=" + generalUtils.sanitise(batchCode) + "&p2=\" + transactionDate + \"&p3=\" + escape(docCode) + \"&p4=\" + escape(accCode) "
                         + "+\"&p5=\" + amount + \"&p6=\" + drCr + \"&p8=\" + escape(remark) "
                         + "+\"&p9=\" + journal + \"&p10=\" + currency + \"&p11=\" + baseAmount +\"&p12=\" + newOrEdit + \"&p13="  + year + "&dnm=\" " + "+ escape('" + dnm + "');");
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

    scoutln(out, bytesOut, "function del(date,accCode){");
    scoutln(out, bytesOut, "var url = \"http://" + men + "/central/servlet/AccountsBatchEntryDelete?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid
                         + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('" + den + "') + \"&p1="
                         + generalUtils.sanitise(batchCode) + "&p4=" + year + "&bnm=\" + escape('" + bnm
                         + "') + \"&p2=\" + escape(date) + \"&p3=\" + escape(accCode) + \"&dnm=\" + escape('" + dnm + "');");
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

    scoutln(out, bytesOut, "function edit(date,accCode)");
    scoutln(out, bytesOut, "{var url = \"http://" + men + "/central/servlet/AccountsBatchEntriesEdit?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men
                         + "') + \"&den=\" + escape('" + den + "') + \"&p1=" + generalUtils.sanitise(batchCode) + "&bnm=\" + escape('" + bnm
                         + "') + \"&p2=\" + date + \"&p3=\" + accCode + \"&p4=" + year + "&dnm=\" + escape('" + dnm + "');");
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

    scoutln(out, bytesOut, "var transactionDate=req2.responseXML.getElementsByTagName(\"transactionDate\")[0].childNodes[0].nodeValue;document.forms[0].transactionDate.value=transactionDate;");

    scoutln(out, bytesOut, "var docCode=req2.responseXML.getElementsByTagName(\"docCode\")[0].childNodes[0].nodeValue;if(docCode=='.')docCode='';document.forms[0].docCode.value=docCode;");
    scoutln(out, bytesOut, "var accCode=req2.responseXML.getElementsByTagName(\"accCode\")[0].childNodes[0].nodeValue;if(accCode=='.')accCode='';document.forms[0].accCode.value=accCode;");
    scoutln(out, bytesOut, "var amount=req2.responseXML.getElementsByTagName(\"amount\")[0].childNodes[0].nodeValue;if(amount=='.')amount='';document.forms[0].amount.value=amount;");
    scoutln(out, bytesOut, "var remark=req2.responseXML.getElementsByTagName(\"remark\")[0].childNodes[0].nodeValue;if(remark=='.')remark='';document.forms[0].remark.value=remark;");
    scoutln(out, bytesOut, "var currency=req2.responseXML.getElementsByTagName(\"currency\")[0].childNodes[0].nodeValue;if(currency=='.')currency='';document.forms[0].currency.value=currency;");

    scoutln(out, bytesOut, "var baseAmount=req2.responseXML.getElementsByTagName(\"baseAmount\")[0].childNodes[0].nodeValue;if(baseAmount=='.')baseAmount='';document.forms[0].baseAmount.value=baseAmount;");

    scoutln(out, bytesOut, "var drCr=req2.responseXML.getElementsByTagName(\"drCr\")[0].childNodes[0].nodeValue;if(drCr=='D')document.forms[0].drCr[0].checked=true;else document.forms[0].drCr[1].checked=true;");
    
    scoutln(out, bytesOut, "var dlm=req2.responseXML.getElementsByTagName(\"dlm\")[0].childNodes[0].nodeValue;document.forms[0].dlm.value=dlm;");

    scoutln(out, bytesOut, "var messageElement = document.getElementById('option');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><a href=\"javascript:updateE()\">Update the Entry</a>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('transactionDate');");
    scoutln(out, bytesOut, "messageElement.innerHTML='<p><input type=hidden name=transactionDate value=\"'+transactionDate+'\">'+transactionDate;");
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
    scoutln(out, bytesOut, "this.location.replace(\"/central/servlet/AccountsBatchEntries?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p1=" + generalUtils.sanitise(batchCode) + "&p2=" + p2 + "&bnm=" + bnm + "\");}");
    
    scoutln(out, bytesOut, "</script>");

    int[] hmenuCount = new int[1];
 
    pageFrameUtils.outputPageFrame(con, stmt, rs, out, req, "6023", "", "AccountsBatchEntries", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    pageFrameUtils.drawTitle(out,  false, false, "", "", "", "", "", "","Accounts - Journal Batch: " + year + " - " + batchCode, "6023", unm, sid, uty, men, den, dnm, bnm, hmenuCount, bytesOut);
  
    scoutln(out, bytesOut, "<form><table id=\"page\" border=0 width=100%>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=8><span id=\"msg\"></span></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\">");

    scoutln(out, bytesOut, "<td><p>Date &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Account &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Dr/Cr &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Currency &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Amount &nbsp;</td>");

    {
      scoutln(out, bytesOut, "<td><p>Base Amount &nbsp;</td>");
      scoutln(out, bytesOut, "<td><p>Derived Rate &nbsp;</td>");
      scoutln(out, bytesOut, "<td><p>Document Code &nbsp;</td>");
    }

    scoutln(out, bytesOut, "<td><p>Remark &nbsp;</td>");
    scoutln(out, bytesOut, "<td><p>Last Modified &nbsp;</td><td></td></tr>");
        
    list(con, stmt, rs, out, year, batchCode, notOpeningBalances, unm, dnm, bytesOut);
 
    if(serverUtils.passLockCheck(con, stmt, rs, "journalbatch", getBatchDate(batchCode, year, dnm, localDefnsDir, defnsDir), unm))
    {
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=9><hr></td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
      scoutln(out, bytesOut, "<tr><td><span id=\"transactionDate\"><input type=text name=transactionDate size=10 maxlength=10></span></td>");

      getAccounts(out, batchCode, year, dnm, localDefnsDir, defnsDir, bytesOut);

      scoutln(out, bytesOut, "<td nowrap><p><input type=radio name=drCr>&nbsp;&nbsp;Debit");
      scoutln(out, bytesOut, "<br><input type=radio name=drCr>&nbsp;&nbsp;Credit</td>");

      getCurrenciesDDL(out, dnm, localDefnsDir, defnsDir, bytesOut);

      scoutln(out, bytesOut, "<td align=right><input type=text name=amount size=10 maxlength=20></td>");

      {
        scoutln(out, bytesOut, "<td align=right><input type=text name=baseAmount size=10 maxlength=20></td>");
        scoutln(out, bytesOut, "<td></td>"); // derived rate

        scoutln(out, bytesOut, "<td><input type=text name=docCode size=10 maxlength=20></td>");
      }

      scoutln(out, bytesOut, "<td colspan=2><input type=text name=remark size=40 maxlength=80></td>");
    
      scoutln(out, bytesOut, "<tr><td><span id=\"dlm\"><input type=hidden name=dlm></span></td></tr>"); /////////////////
    
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=3><span id=\"option\"><p><a href=\"javascript:updateN()\">Add the Entry</a></span></td></tr>");
    }
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    
    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void list(Connection ofsaCon, Statement ofsaStmt, ResultSet ofsaRs, PrintWriter out, String year, String batchCode, boolean notOpeningBalances, String unm, String dnm, int[] bytesOut) throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT * FROM joubatchl WHERE Code = '" + batchCode + "' ORDER BY TransactionDate, AccCode"); 

      String code, transactionDate, docCode, accCode, amount, drCr, signOn, remark, dlm, journal, currency, baseAmount, cssFormat;
      boolean line1=true;
      double totalDebits = 0.0, totalCredits = 0.0;
      int countDr = 0, countCr = 0;

      while(rs.next())
      {
        code            = rs.getString(1);
        transactionDate = rs.getString(2);
        docCode         = rs.getString(3);
        accCode         = rs.getString(4);
        amount          = rs.getString(5);
        drCr            = rs.getString(6);
        signOn          = rs.getString(7);
        remark          = rs.getString(8);
        journal         = rs.getString(9);
        dlm             = rs.getString(10);
        currency        = rs.getString(11);
        baseAmount      = rs.getString(12);
             
        if(line1) { cssFormat = "line1"; line1 = false; } else { cssFormat = "line2"; line1 = true; }
                    
        scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\">");
    
        scoutln(out, bytesOut, "<td>" + generalUtils.convertFromYYYYMMDD(transactionDate) + "</td>");
        scoutln(out, bytesOut, "<td>" + accCode + "</td>");

        if(drCr.equals("D"))
        {  
          scoutln(out, bytesOut, "<td>Dr</td>");
          totalDebits += generalUtils.doubleDPs(generalUtils.doubleFromStr(baseAmount), '2');
          ++countDr;
        }
        else
        {
          scoutln(out, bytesOut, "<td align=right>Cr</td>");
          totalCredits += generalUtils.doubleDPs(generalUtils.doubleFromStr(baseAmount), '2');
          ++countCr;
        }
        
        scoutln(out, bytesOut, "<td align=right>" + currency + "</td>");
        scoutln(out, bytesOut, "<td align=right>" + generalUtils.formatNumeric(amount, '2') + "</td>");

        {
          scoutln(out, bytesOut, "<td align=right>" + generalUtils.formatNumeric(baseAmount, '2') + "</td>");

          if(generalUtils.doubleFromStr(amount) == 0.0)
            amount = "1.0"; // just-in-case
          scoutln(out, bytesOut, "<td align=right>" + generalUtils.formatNumeric((generalUtils.doubleFromStr(baseAmount) / generalUtils.doubleFromStr(amount)), '8') + "</td>");

          scoutln(out, bytesOut, "<td>" + docCode + "</td>");
        }

        scoutln(out, bytesOut, "<td>" + remark + "</td>");
        
        int x=0;
        while(x < dlm.length() && dlm.charAt(x) != ' ') // just-in-case
          ++x;
        scoutln(out, bytesOut, "<td>" + signOn + " on " + dlm.substring(0, x) +  "</td>");

        if(serverUtils.passLockCheck(ofsaCon, ofsaStmt, ofsaRs, "journalbatch", transactionDate, unm))
        {
          scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:edit('" + generalUtils.convertFromYYYYMMDD(transactionDate) + "','" + accCode + "')\">Edit</a></td>");
          scoutln(out, bytesOut, "<td nowrap><a href=\"javascript:del('" + transactionDate + "','" + accCode + "')\">Delete</a></td></tr>");
        }
        else scoutln(out, bytesOut, "<td></td><td></td></tr>");
      }
                 
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

      scoutln(out, bytesOut, "<tr><td colspan=2><p>Total Debits:</td><td colspan=2 align=right><p>" + generalUtils.formatNumeric(totalDebits, '2') + "</td></tr>");
        scoutln(out, bytesOut, "<tr><td colspan=2><p>Total Credits:</td><td colspan=2 align=right><p>" + generalUtils.formatNumeric(totalCredits, '2') + "</td></tr>");
        scoutln(out, bytesOut, "<tr><td colspan=2><p>Difference:</td><td colspan=2 align=right><p>" + generalUtils.formatNumeric(totalDebits - totalCredits, '2') + "</td></tr>");

      scoutln(out, bytesOut, "<tr><td colspan=2><p>Entries:</td><td colspan=2 align=right><p>" + generalUtils.formatNumeric(countDr + countCr, '0') + " (" + generalUtils.formatNumeric(countDr, '0') + " debits, "  + generalUtils.formatNumeric(countCr, '0')
                           + " credits)</td></tr>");
      
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getAccounts(PrintWriter out, String batchCode, String year, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                           throws Exception
  {
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
    
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();

      Class.forName("com.mysql.jdbc.Driver").newInstance();
    
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Type FROM joubatch WHERE Code = '" + batchCode + "'"); 

      String type = "";

      if(rs.next()) // just-in-case
      {
        type = rs.getString(1);

        if(type.equals("D"))
          getAccountsDebtorsDDL(out, dnm, localDefnsDir, defnsDir, bytesOut);
        else
        if(type.equals("C"))
          getAccountsCreditorsDDL(out, dnm, localDefnsDir, defnsDir, bytesOut);
        else getAccountsGeneralDDL(out, year, dnm, localDefnsDir, defnsDir, bytesOut);
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

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getAccountsGeneralDDL(PrintWriter out, String year, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut)
                                     throws Exception
  {
    String uName = directoryUtils.getMySQLUserName();
    String pWord = directoryUtils.getMySQLPassWord();
    
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT AccCode FROM acctdefn ORDER BY AccCode"); 

    scoutln(out, bytesOut, "<td><span id=\"accCode\"><select name=\"accCode\">");
    
    String accCode;
    
    while(rs.next())
    {
      accCode = rs.getString(1);
      scoutln(out, bytesOut, "<option value=\"" + accCode + "\">" + accCode);
    }

    scoutln(out, bytesOut, "</select></span></td>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getAccountsDebtorsDDL(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT CompanyCode FROM company ORDER BY CompanyCode");

    scoutln(out, bytesOut, "<td><span id=\"accCode\"><select name=\"accCode\">");
    
    String accCode;
    
    while(rs.next())
    {
      accCode = rs.getString(1);
      scoutln(out, bytesOut, "<option value=\"" + accCode + "\">" + accCode);
    }

    scoutln(out, bytesOut, "</select></span></td>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getAccountsCreditorsDDL(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT SupplierCode FROM supplier ORDER BY SupplierCode");

    scoutln(out, bytesOut, "<td><span id=\"accCode\"><select name=\"accCode\">");
    
    String accCode;
    
    while(rs.next())
    {
      accCode = rs.getString(1);
      scoutln(out, bytesOut, "<option value=\"" + accCode + "\">" + accCode);
    }

    scoutln(out, bytesOut, "</select></span></td>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void getCurrenciesDDL(PrintWriter out, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = con.createStatement();

    ResultSet rs = stmt.executeQuery("SELECT CurrencyCode, CurrencyDesc FROM currency ORDER BY CurrencyCode"); 

    scoutln(out, bytesOut, "<td align=right><select name=\"currency\">");
    
    String currency;
    
    while(rs.next())
    {
      currency = rs.getString(1);
      scoutln(out, bytesOut, "<option value=\"" + currency + "\">" + currency);
    }

    scoutln(out, bytesOut, "</select></td>");
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private String getBatchDate(String batchCode, String year, String dnm, String localDefnsDir, String defnsDir) throws Exception
  {
    String date = "2099-12-31";
    
    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;
            
    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);
      
      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT Date FROM joubatch WHERE Code = '" + generalUtils.sanitiseForSQL(batchCode) + "'"); 

      if(rs.next())
        date = rs.getString(1);
                 
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
    
    return date;
  }  
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private boolean areNotOpeningBalances(String batchCode, String year, String dnm) throws Exception
  {
    String ob = "Y";

    Connection con = null;
    Statement stmt = null;
    ResultSet rs   = null;

    try
    {
      String uName = directoryUtils.getMySQLUserName();
      String pWord = directoryUtils.getMySQLPassWord();
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_accounts_" + year + "?user=" + uName + "&password=" + pWord);

      stmt = con.createStatement();

      rs = stmt.executeQuery("SELECT OpeningBalances FROM joubatch WHERE Code = '" + generalUtils.sanitiseForSQL(batchCode) + "'");

      if(rs.next())
        ob = rs.getString(1);

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

    if(ob.equals("Y"))
      return false;

    return true;
  }

}
