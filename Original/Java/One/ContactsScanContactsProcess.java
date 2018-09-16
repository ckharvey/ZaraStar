// =======================================================================================================================================================================================================
// System: ZaraStar: Contacts: scan contacts
// Module: ContactsScanContactsProcess.java
// Author: C.K.Harvey
// Copyright (c) 1999-2008 Christopher Harvey. All Rights Reserved.
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
import java.util.*;
import java.sql.*;

public class ContactsScanContactsProcess extends HttpServlet
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
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", words="", personName="", companyName="", eMail="", companyCode="";

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
      words  = req.getParameter("words");
      personName  = req.getParameter("personName");
      companyName  = req.getParameter("companyName");
      eMail  = req.getParameter("eMail");
      companyCode  = req.getParameter("companyCode");

      words = generalUtils.stripLeadingAndTrailingSpaces(words);
              
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, words, personName, companyName, eMail, companyCode, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ContactsScanContactsProcess", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8805, bytesOut[0], 0, "ERR:" + words);
      if(out != null) out.flush();
    }
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", words="", personName="", companyName="", eMail="", companyCode="";

    try
    {
      directoryUtils.setContentHeaders(res);
      out = res.getWriter();

      Enumeration en = req.getParameterNames();
      while(en.hasMoreElements())
      {
        String name = (String)en.nextElement();
        String[] value = req.getParameterValues(name);
        if(name.equals("unm"))
          unm = value[0];
        else
        if(name.equals("sid"))
          sid = value[0];
        else
        if(name.equals("uty"))
          uty = value[0];
        else
        if(name.equals("men"))
          men = value[0];
        else
        if(name.equals("den"))
          den = value[0];
        else
        if(name.equals("dnm"))
          dnm = value[0];
        else
        if(name.equals("bnm"))
          bnm = value[0];
        else
        if(name.equals("words")) 
          words = value[0];
        else
        if(name.equals("personName")) 
          personName = value[0];
        else
        if(name.equals("companyName"))
          companyName = value[0];
        else
        if(name.equals("eMail")) 
          eMail = value[0];
        else
        if(name.equals("companyCode"))
          companyCode = value[0];
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".x")) ;
        else
        if(name.length() > 2 && name.substring(1, 3).equals(".y")) ;
      }  

      words = generalUtils.stripLeadingAndTrailingSpaces(words);
              
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, words, personName, companyName, eMail, companyCode, bytesOut);
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

      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ContactsScanContactsProcess", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8805, bytesOut[0], 0, "ERR:" + words);
      if(out != null) out.flush();
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String words, String personName, String companyName, String eMail, String companyCode, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);
    String imagesDir     = directoryUtils.getSupportDirs('I');

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "8805", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8805, bytesOut[0], 0, "ACC:" + words);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "8805a", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8805, bytesOut[0], 0, "SID:" + words);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }
    
    set(con, stmt, rs, out, req, words, personName, companyName, eMail, companyCode, unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);
            
    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8805, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), personName);
    if(con != null) con.close();
    if(out != null) out.flush();
  }
   
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String words, String personName,
                   String companyName, String eMail, String companyCode, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Scan Contacts</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    
    scoutln(out, bytesOut, "function scan(){document.forms[0].submit()}");
    
    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 4001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewCust(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/CustomerPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    if(authenticationUtils.verifyAccess(con, stmt, rs, req, 5001, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      scoutln(out, bytesOut, "function viewSupp(code){var p1=sanitise(code);");
      scoutln(out, bytesOut, "this.location.href=\"/central/servlet/SupplierPage?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=A&p1=\"+p1;}");
    }

    scoutln(out, bytesOut, "function update(code,type,contactCode){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ContactsCompanyUpdate?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                           + "&dnm=" + dnm + "&bnm=" + bnm + "&words=" + generalUtils.sanitise(words) + "&personName=" + generalUtils.sanitise(personName)
                           + "&companyName=" + generalUtils.sanitise(companyName) + "&eMail=" + generalUtils.sanitise(eMail) + "&companyCode="
                           + generalUtils.sanitise(companyCode) + "&p3=\"+contactCode+\"&p2=\"+type+\"&p1=\"+code;}");

    scoutln(out, bytesOut, "function fetch(contactCode,companyName,owner){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/ContactsDetails?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&p3=&p1=\"+contactCode+\"&p2=\"+companyName+\"&p4=\"+owner+\"&bnm=" + bnm + "\";}");

    contactsUtils.outputJS(con, stmt, rs, out, req, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

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

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
    
    contactsUtils.outputPageFrame(con, stmt, rs, out, req, "", "ContactsScanContacts", "8805", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
  
    contactsUtils.drawTitle(con, stmt, rs, req, out, "Scan Contacts", "8805", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    
    scoutln(out, bytesOut, "<form action=\"ContactsScanContactsProcess\" enctype=\"application/x-www-form-urlencoded\" method=post>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"unm\" value='" + unm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"sid\" value='" + sid + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"uty\" value='" + uty + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"men\" value='" + men + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"den\" value='" + den + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"dnm\" value='" + dnm + "'>");
    scoutln(out, bytesOut, "<input type=\"hidden\" name=\"bnm\" value='" + bnm + "'>");
    
    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0>");
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>Owner</td><td><p>Person Name</td><td><p>Contact Code</td><td><p>Company Name</td>");
    scoutln(out, bytesOut, "<td><p>eMail</td><td><p>Customer Code</td><td><p>Supplier Code</td><td><p>External Code</td></tr>");

    scan(con, stmt, rs, out, words, personName, companyName, eMail, companyCode, bytesOut);
      
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");

    scoutln(out, bytesOut, "</table></form>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scan(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String words, String personName, String companyName, String eMail,
                    String companyCode, int[] bytesOut) throws Exception
  {
    int x=0;
    String s, word;
    String cssFormat="";
  
    int len = words.length();
  
    while(x < len)
    {
      word = "";
      while(x < len && words.charAt(x) != ' ')
        word += words.charAt(x++);
      ++x;
      while(x < len && words.charAt(x) == ' ')
        ++x;
      
      stmt = con.createStatement();
    
      word = generalUtils.sanitiseForSQL(word);
      
      s = "";
      if(personName.equals("on"))
        s += "Name LIKE '%" + word + "%' ";
      
      if(companyName.equals("on"))
      {
        if(s.length() > 0)
          s += "OR ";
        s += "CompanyName LIKE '%" + word + "%' ";
      }
      
      if(eMail.equals("on"))
      {
        if(s.length() > 0)
          s += "OR ";
        s += "EMail LIKE '%" + word + "%' ";      
      }
      
      rs = stmt.executeQuery("SELECT ContactCode, Owner, Name, CompanyName, EMail, CustomerCode, SupplierCode, ExternalCode FROM contacts WHERE " + s
                           + " ORDER BY EMail");

      String contactCode, owner, name, compName, eM, custCode, suppCode, extCode;
      
      while(rs.next())
      {    
        contactCode = rs.getString(1);
        owner       = rs.getString(2);
        name        = rs.getString(3);
        compName    = rs.getString(4);
        eM          = rs.getString(5);
        custCode    = rs.getString(6);
        suppCode    = rs.getString(7);
        extCode     = rs.getString(8);
             
        if(name     == null) name = "";
        if(compName == null) compName = "";
        if(eM       == null) eM = "";
        if(custCode == null) custCode = "";
        if(suppCode == null) suppCode = "";
        if(extCode  == null) extCode = "";
        
        if(cssFormat.equals("line1"))
          cssFormat = "line2";
        else cssFormat = "line1"; 

        scout(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td><p>" + owner + "</td>");

        scoutln(out, bytesOut, "<td nowrap><p><a href=\"javascript:fetch('" + contactCode + "','" + generalUtils.sanitise(compName) + "','"
                             + generalUtils.sanitise(owner) + "')\">" + contactCode + "</a></td>");
        
        scout(out, bytesOut, "<td><p>" + highlightWord(word, name) + "</td>");
        scout(out, bytesOut, "<td><p>" + highlightWord(word, compName) + "</td>");
        scout(out, bytesOut, "<td><p>" + highlightWord(word, eM) + "</td>");
        
        if(custCode.length() == 0)
          scoutln(out, bytesOut, "<td><p><a href=\"javascript:update('" + companyCode + "','C','" + contactCode + "')\">Set to " + companyCode + "</a></td>");
        else scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewCust('" + custCode + "')\">" + custCode + "</a></td>");
        
        if(suppCode.length() == 0)
          scoutln(out, bytesOut, "<td><p><a href=\"javascript:update('" + companyCode + "','S','" + contactCode + "')\">Set to " + companyCode + "</a></td>");
        else scoutln(out, bytesOut, "<td><p><a href=\"javascript:viewSupp('" + suppCode + "')\">" + suppCode + "</a></td>");
        
        scoutln(out, bytesOut, "<td><p>" + extCode + "</td></tr>");
      }
      
      if(rs   != null) rs.close();
      if(stmt != null) stmt.close();
    }
  }
  
  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private String highlightWord(String word, String s) throws Exception
  {
    int i = (s.toUpperCase()).indexOf(word.toUpperCase());
    if(i != -1)
      return s.substring(0, i) + "<font color=red>" + s.substring(i, (i + word.length())) + "</font>" + s.substring(i + word.length());      
    return s;
  }
          
  // -------------------------------------------------------------------------------------------------------------------------------------------------
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
