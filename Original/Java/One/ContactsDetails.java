// =======================================================================================================================================================================================================
// System: ZaraStar: Contacts: contact details
// Module: ContactsDetails.java
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

public class ContactsDetails extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  Profile profile = new Profile();
  ContactsUtils contactsUtils = new ContactsUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="";
                         
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
      p1  = req.getParameter("p1"); // contactCode
      p2  = req.getParameter("p2"); // externalCode (set if coming from 8817 - select)
      p3  = req.getParameter("p3"); // returnToWhere (set if coming from 6301a - MLM list)
      p4  = req.getParameter("p4"); // owner
      
      if(p1 == null) p1 = "";
      if(p2 == null) p2 = "";
      if(p3 == null) p3 = "";
      if(p4 == null) p4 = unm;
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, bytesOut);
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

      System.out.println("8811: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ContactsDetails", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8811, bytesOut[0], 0, "ERR:");
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, String p4, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null, stmt2 = null;
    ResultSet rs   = null, rs2 = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ContactsDetails", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8811, bytesOut[0], 0, "ACC:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ContactsDetails", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8811, bytesOut[0], 0, "SID:");
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, stmt2, rs, rs2, out, req, p1, p3, p4, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8811, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, Statement stmt2, ResultSet rs, ResultSet rs2, PrintWriter out, HttpServletRequest req, String contactCode, String returnToWhere, String p4, String unm, String sid, String uty, String men,
                   String den, String dnm, String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    if(contactCode.length() == 0)
      scoutln(out, bytesOut, "<html><head><title>Add Contact</title>");
    else scoutln(out, bytesOut, "<html><head><title>Update Contact</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    
    contactsUtils.outputJS(con, stmt, rs, out, req, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8819, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function create(){");
      scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ContactsExternalAccess?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=" + generalUtils.sanitise(contactCode) + "\";}");
    }
    
    // get from clipboard
    scoutln(out, bytesOut, "var req1;");
    scoutln(out, bytesOut, "function initRequest1(url){");
    scoutln(out, bytesOut, "if(window.XMLHttpRequest){req1=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req1=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function fromCB(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/ClipboardContents?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest1(url);");
    scoutln(out, bytesOut, "req1.onreadystatechange=processRequest1;");
    scoutln(out, bytesOut, "req1.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req1.send(null);}");

    scoutln(out, bytesOut, "function processRequest1(){");
    scoutln(out, bytesOut, "if(req1.readyState==4){");
    scoutln(out, bytesOut, "if(req1.status==200){");
    scoutln(out, bytesOut, "var res=req1.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length>0){");
    scoutln(out, bytesOut, "if(res=='.'){");

    scoutln(out, bytesOut, "var val=req1.responseXML.getElementsByTagName(\"val\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var len=val.length;var y=0;");
    scoutln(out, bytesOut, "var extCode='';while(y<len&&val.charAt(y)!=' ')extCode+=val.charAt(y++);++y;");
    scoutln(out, bytesOut, "var extPwd='';while(y<len&&val.charAt(y)!=' ')extPwd+=val.charAt(y++);++y;");
    scoutln(out, bytesOut, "var extRights='';while(y<len&&val.charAt(y)!=' ')extRights+=val.charAt(y++);++y;");
    scoutln(out, bytesOut, "var extApp='';while(y<len&&val.charAt(y)!=' ')extApp+=val.charAt(y++);");

    scoutln(out, bytesOut, "var messageElement=document.getElementById('externalCode');messageElement.innerHTML=extCode;");
    scoutln(out, bytesOut, "messageElement=document.getElementById('externalPassWord');messageElement.innerHTML=extPwd;");
    scoutln(out, bytesOut, "messageElement=document.getElementById('externalRights');//messageElement.innerHTML=extRights;");
    scoutln(out, bytesOut, "messageElement=document.getElementById('externalApproved');messageElement.innerHTML=extApp;");
    scoutln(out, bytesOut, "messageElement=document.getElementById('hiddenExternalCode');messageElement.innerHTML='<input type=hidden name=hiddenExternalCode value='+extCode+'>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('hiddenExternalPassWord');messageElement.innerHTML='<input type=hidden name=hiddenExternalPassWord value='+extPwd+'>';");

    scoutln(out, bytesOut, "}}}}}");
    
    // delete ext access rec
    scoutln(out, bytesOut, "var req3;");    
    scoutln(out, bytesOut, "function initRequest3(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req3=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req3=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function del(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/ContactsExternalAccessDelete?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape('" + contactCode + "') + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest3(url);");
    scoutln(out, bytesOut, "req3.onreadystatechange=processRequest3;");
    scoutln(out, bytesOut, "req3.open(\"GET\", url, true);");
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
    
    // create new ext access rec
    scoutln(out, bytesOut, "var req2;");    
    scoutln(out, bytesOut, "function initRequest2(url)");
    scoutln(out, bytesOut, "{if(window.XMLHttpRequest){req2=new XMLHttpRequest();}else");
    scoutln(out, bytesOut, "if(window.ActiveXObject){req2=new ActiveXObject(\"Microsoft.XMLHTTP\");}}");

    scoutln(out, bytesOut, "function create(){");
    scoutln(out, bytesOut, "var url=\"http://" + men + "/central/servlet/ContactsExternalAccess?unm=\" + escape('" + unm + "') + \"&sid=\" + escape('" + sid + "') + \"&uty=\" + escape('" + uty + "') + \"&men=\" + escape('" + men + "') + \"&den=\" + escape('"
                         + den + "') + \"&bnm=\" + escape('" + bnm + "') + \"&p1=\" + escape('" + contactCode + "') + \"&dnm=\" + escape('" + dnm + "');");
    scoutln(out, bytesOut, "initRequest2(url);");
    scoutln(out, bytesOut, "req2.onreadystatechange=processRequest2;");
    scoutln(out, bytesOut, "req2.open(\"GET\",url,true);");
    scoutln(out, bytesOut, "req2.send(null);}");

    scoutln(out, bytesOut, "function processRequest2(){");
    scoutln(out, bytesOut, "if(req2.readyState==4){");
    scoutln(out, bytesOut, "if(req2.status == 200){");
    scoutln(out, bytesOut, "var res=req2.responseXML.getElementsByTagName(\"res\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "if(res.length > 0){");
    scoutln(out, bytesOut, "if(res=='.'){");
    scoutln(out, bytesOut, "var code=req2.responseXML.getElementsByTagName(\"code\")[0].childNodes[0].nodeValue;");
    scoutln(out, bytesOut, "var pwd=req2.responseXML.getElementsByTagName(\"pwd\")[0].childNodes[0].nodeValue;");

    scoutln(out, bytesOut, "var messageElement=document.getElementById('externalCode');messageElement.innerHTML=code;");
    scoutln(out, bytesOut, "messageElement=document.getElementById('hiddenExternalCode');messageElement.innerHTML='<input type=hidden name=hiddenExternalCode value='+code+'>';");
    scoutln(out, bytesOut, "messageElement=document.getElementById('externalPassWord');messageElement.innerHTML=pwd;");
    scoutln(out, bytesOut, "messageElement=document.getElementById('hiddenExternalPassWord');messageElement.innerHTML='<input type=hidden name=hiddenExternalPassWord value='+pwd+'>';");

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

    scoutln(out, bytesOut, "function save(){document.forms[0].submit()}");
    if(contactCode.length() != 0)
      scoutln(out, bytesOut, "function remove(){document.forms[0].newOrEditOrDelete.value='D';document.forms[0].submit();}");
    
    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
    
    contactsUtils.outputPageFrame(con, stmt, rs, out, req, "fromCB();", "ContactsDetails", "8811", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    if(contactCode.length() == 0)
      contactsUtils.drawTitle(con, stmt, rs, req, out, "Add New Contact", "8811", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    else contactsUtils.drawTitle(con, stmt, rs, req, out, "Update Contact Details", "8811", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
    scoutln(out, bytesOut, "<form action=\"ContactsDetailsUpdate\" enctype=\"application/x-www-form-urlencoded\" method=post>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"contactCode\" value='" + contactCode + "'>");
    if(contactCode.length() == 0)
      scoutln(out, bytesOut, "<input type=\"hidden\" name=\"newOrEditOrDelete\" value='N'>");
    else scoutln(out, bytesOut, "<input type=\"hidden\" name=\"newOrEditOrDelete\" value='E'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"returnToWhere\" value='" + returnToWhere + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"userCode\" value='" + p4 + "'>");

    String[] owner            = new String[1];
    String[] name             = new String[1];
    String[] companyName      = new String[1];
    String[] eMail            = new String[1];
    String[] userCode         = new String[1];
    String[] domain           = new String[1];
    String[] jobTitle         = new String[1];
    String[] notes            = new String[1];
    String[] customerCode     = new String[1];
    String[] supplierCode     = new String[1];
    String[] organizationCode = new String[1];
    String[] externalCode     = new String[1];
    String[] externalPassWord = new String[1];
    String[] externalRights   = new String[1];
    String[] externalApproved = new String[1];
    String[] phone1           = new String[1];
    String[] phone2           = new String[1];
    String[] phone3           = new String[1];
    String[] fax              = new String[1];
    String[] mailingList      = new String[1];
    
    profile.getContact(contactCode, owner, name, companyName, eMail, domain, userCode, jobTitle, notes, customerCode, supplierCode, organizationCode, externalCode, externalPassWord, externalRights, externalApproved, phone1, phone2, phone3, fax,
                     mailingList, dnm, localDefnsDir, defnsDir);

    if(externalApproved[0].equals("Y"))
      externalApproved[0] = "Yes";
    else externalApproved[0] = "No";
              
    scoutln(out, bytesOut, "<span id=hiddenExternalCode><input type=\"hidden\" name=\"hiddenExternalCode\" value='" + externalCode[0] + "'></span>");

    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Contact Code:</td><td><p>" + contactCode + "</td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Person Name:</td><td colspan=3><p><input type=text name=name size=60 maxlength=60 value=\"" + name[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Company Name:</td><td colspan=3><p><input type=text name=companyName size=60 maxlength=100 value=\"" + companyName[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Job Title:</td><td colspan=3><p><input type=text name=jobTitle size=60 maxlength=80 value=\"" + jobTitle[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>eMail:</td><td colspan=3><p><input type=text name=eMail size=60 maxlength=60 value=\"" + eMail[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Customer Code:</td><td colspan=3><p><input type=text name=customerCode size=20 maxlength=20 value=\"" + customerCode[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Supplier Code:</td><td colspan=3><p><input type=text name=supplierCode size=20 maxlength=20 value=\"" + supplierCode[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Phones:</td><td colspan=3><p><input type=text name=phone1 size=15 maxlength=30 value=\"" + phone1[0] + "\">");
    scoutln(out, bytesOut, "&nbsp;&nbsp;<input type=text name=phone2 size=15 maxlength=30 value=\"" + phone2[0] + "\">");
    scoutln(out, bytesOut, "&nbsp;&nbsp;<input type=text name=phone3 size=15 maxlength=30 value=\"" + phone3[0] + "\"></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>Fax:</td><td colspan=3><p><input type=text name=fax size=15 maxlength=30 value=\"" + fax[0] + "\"></td></tr>");

    scout(out, bytesOut, "<tr><td nowrap><p>Mailing List:</td><td><p><input type=checkbox name=mailingList ");
    if(mailingList[0].equals("Y"))
      scout(out, bytesOut, " checked");
    scoutln(out, bytesOut, "></td></tr>");

    scoutln(out, bytesOut, "<tr><td><p>External Access Code:</td><td colspan=2><p><span id=externalCode>" + externalCode[0] + "</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8819, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      if(externalCode[0].length() == 0)
        scoutln(out, bytesOut, "<a href=\"javascript:create()\">Create</a>");
      else scoutln(out, bytesOut, "<a href=\"javascript:del()\">Delete</a>");
    }
    scoutln(out, bytesOut, "</td></tr>");

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8819, unm, uty, dnm, localDefnsDir, defnsDir))
      scoutln(out, bytesOut, "<tr><td><p>External Access PassWord:</td><td><p><input type=text name=hiddenExternalPassWord size=20 maxlength=20 value=\"" + externalPassWord[0] + "\"></td></tr>");
    else
    {
       scoutln(out, bytesOut, "<tr><td><p>External Access PassWord:</td><td><p><span id=externalPassWord>" + externalPassWord[0] + "</span></td></tr>");
       scoutln(out, bytesOut, "<span id=hiddenExternalPassWord><input type=\"hidden\" name=\"hiddenExternalPassWord\" value='" + externalPassWord[0] + "'></span>");
    }
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 8819, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "<tr><td><p>External Access Rights:</td><td><p>");
      setRights(out, externalRights[0], bytesOut);
      scoutln(out, bytesOut, "</td></tr>");    
    }
    else scoutln(out, bytesOut, "<tr><td><input type=hidden name=externalRights value=\"" + externalRights[0] + "\"></td></tr>");
    
    scoutln(out, bytesOut, "<tr><td><p>External Access Approved:</td><td><p><span id=externalApproved>" + externalApproved[0] + "</span></td></tr>");
        
    scoutln(out, bytesOut, "<tr><td valign=top nowrap><p>Notes:</td><td colspan=2><p><textarea name=notes cols=60 rows=10>" + notes[0] + "</textarea></td></tr>");

    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    if(contactCode.length() == 0)
      scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:save()\">Add the Contact</a></td></tr>");
    else
    {
      scoutln(out, bytesOut, "<tr><td></td><td><p><a href=\"javascript:save()\">Update the Contact</a></td></tr>");
      scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=4><hr></td></tr>");
      scoutln(out, bytesOut, "<tr><td colspan=4 align=right><p><a href=\"javascript:remove()\">Delete the Contact</a></td></tr>");
    }

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setRights(PrintWriter out, String thisOption, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<select name=\"externalRights\">");
      
    scout(out, bytesOut, "<option value=\"accounts\"");
    if(thisOption.equals("accounts"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">Accounts");      
    
    scout(out, bytesOut, "<option value=\"sales\"");
    if(thisOption.equals("sales"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">Sales");      
    
    scout(out, bytesOut, "<option value=\"store\"");
    if(thisOption.equals("store"))
      scout(out, bytesOut, " selected");
    scoutln(out, bytesOut, ">Store");      
    
    scoutln(out, bytesOut, "</select>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();
  }
  private void scoutln(PrintWriter out, int[] bytesOut, String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
