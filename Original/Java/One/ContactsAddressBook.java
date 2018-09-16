// =======================================================================================================================================================================================================
// System: ZaraStar: Contacts: Address Book
// Module: ContactsAddressBook.java
// Author: C.K.Harvey
// Copyright (c) 2000-09 Christopher Harvey. All Rights Reserved.
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

public class ContactsAddressBook extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  ContactsUtils contactsUtils = new ContactsUtils();
  
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
      sid = req.getParameter("sid");
      uty = req.getParameter("uty");
      men = req.getParameter("men");
      den = req.getParameter("den");
      dnm = req.getParameter("dnm");
      bnm = req.getParameter("bnm");
      p1  = req.getParameter("p1"); // searchChar
      p2  = req.getParameter("p2"); // which
      p3  = req.getParameter("p3"); // userCode
      
      if(p1 == null) p1 = " ";
      if(p2 == null) p2 = "N"; // Name
      if(p3 == null) p3 = unm;

      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, bytesOut);
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

      System.out.println("8801: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "ContactsAddressBook", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8801, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm, String p1, String p2, String p3, int[] bytesOut) throws Exception
  {
    long startTime = new java.util.Date().getTime();

    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8801, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "ContactsAddressBook", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8801, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "ContactsAddressBook", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8801, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    set(con, stmt, rs, out, req, p1.charAt(0), p2, p3, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8801, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con != null) con.close();
    if(out != null) out.flush();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, char searchChar, String type, String userCode, String unm, String sid, String uty, String men, String den, String dnm,
                   String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Address Book</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");
    
    scoutln(out, bytesOut, "function select(ch){");
    scoutln(out, bytesOut, "window.location.replace(\"/central/servlet/ContactsAddressBook?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=N&p3=" + generalUtils.sanitise(userCode) + "&p1=\"+ch);}");

    scoutln(out, bytesOut, "function select2(ch){");
    scoutln(out, bytesOut, "window.location.replace(\"/central/servlet/ContactsAddressBook?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "&p2=C&p3=" + generalUtils.sanitise(userCode) + "&p1=\"+ch);}");

    scoutln(out, bytesOut, "function add(){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ContactsDetails?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function upload(){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ContactsAddressBookUpload?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&bnm=" + bnm + "\";}");

    scoutln(out, bytesOut, "function edit(contactCode){");
    scoutln(out, bytesOut, "window.location.href=\"/central/servlet/ContactsDetails?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den + "&dnm=" + dnm + "&p4=" + userCode + "&p1=\"+contactCode+\"&bnm=" + bnm + "\";}");

    contactsUtils.outputJS(con, stmt, rs, out, req, unm, uty, sid, men, den, dnm, bnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];

    contactsUtils.outputPageFrame(con, stmt, rs, out, req, "", "ContactsAddressBook", "8801", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
 
    if(userCode.equals(unm))
      contactsUtils.drawTitle(con, stmt, rs, req, out, "Contacts: My Address Book", "8801", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);
    else contactsUtils.drawTitle(con, stmt, rs, req, out, "Contacts: Address Book (" + userCode + ")", "8801", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    setHead(con, stmt, rs, out, userCode, imagesDir, bytesOut);
  
    scoutln(out, bytesOut, "<table id='page' width=100% border=0>");

    boolean viewOnly = true;
    
    if(unm.equals("Sysadmin") || userCode.equals(unm))
      viewOnly = false;
    else
    {
      String[] mode = new String[1];
      Profile profile = new Profile();
      profile.areContactsShared(con, stmt, rs, unm, userCode, mode);
      
      if(mode[0].equals("E"))
        viewOnly = false;  
    }
    
    String typeField;
    if(type.equals("C"))
    {
      if(searchChar == '-')
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>Code</td><td><p>Company Name<br>(Others)</td><td><p>Person Name</td><td><p>Customer</td><td><p>Supplier</td>");
      else scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>Code</td><td><p>Company Name<br>Starting with " + searchChar + "</td><td><p>Person Name</td><td><p>Customer</td><td><p>Supplier</td>");
      
      typeField = "CompanyName";
    }
    else
    {
      if(searchChar == '-')
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>Code</td><td><p>Person Name<br>(Others)</td><td><p>Company Name</td><td><p>Customer</td><td><p>Supplier</td>");
      else scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td><p>Code</td><td><p>Person Name<br>Starting with " + searchChar + "</td><td><p>Company Name</td><td><p>Customer</td><td><p>Supplier</td>");
      
      typeField = "Name";
    }

    scoutln(out, bytesOut, "<td><p>eMail</td></tr>");

    if(searchChar == ' ')
      ;
    else
    if(searchChar == '-')
      searchOthers(viewOnly, con, stmt, rs, out, typeField, userCode, bytesOut);
    else search(viewOnly, con, stmt, rs, out, searchChar, typeField, userCode, bytesOut);

    scoutln(out, bytesOut, "</table>");
    scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void search(boolean viewOnly, Connection con, Statement stmt, ResultSet rs, PrintWriter out, char searchChar, String typeField, String userCode, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ContactCode, Name, EMail, CompanyName, CustomerCode, SupplierCode FROM contacts WHERE Owner = '" + userCode + "' AND " + typeField + " LIKE '" + searchChar + "%' ORDER BY " + typeField);

    String cssFormat = "";

    while(rs.next())
    {    
      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";
      
      if(typeField.equals("Name"))
        writeBodyLine(viewOnly, out, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), cssFormat, bytesOut);
      else writeBodyLine2(viewOnly, out, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), cssFormat, bytesOut);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void searchOthers(boolean viewOnly, Connection con, Statement stmt, ResultSet rs, PrintWriter out, String typeField, String userCode, int[] bytesOut) throws Exception
  {
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT ContactCode, Name, EMail, CompanyName, CustomerCode, SupplierCode FROM contacts WHERE Owner = '" + userCode + "' AND " + typeField
                         + " NOT LIKE 'A%' AND " + typeField + " NOT LIKE 'B%' AND " + typeField + " NOT LIKE 'C%' AND " + typeField
                         + " NOT LIKE 'D%' AND " + typeField + " NOT LIKE 'E%' AND " + typeField + " NOT LIKE 'F%' AND " + typeField
                         + " NOT LIKE 'G%' AND " + typeField + " NOT LIKE 'H%' AND " + typeField + " NOT LIKE 'I%' AND " + typeField
                         + " NOT LIKE 'J%' AND " + typeField + " NOT LIKE 'K%' AND " + typeField + " NOT LIKE 'L%' AND " + typeField
                         + " NOT LIKE 'M%' AND " + typeField + " NOT LIKE 'N%' AND " + typeField + " NOT LIKE 'O%' AND " + typeField
                         + " NOT LIKE 'P%' AND " + typeField + " NOT LIKE 'Q%' AND " + typeField + " NOT LIKE 'R%' AND " + typeField
                         + " NOT LIKE 'S%' AND " + typeField + " NOT LIKE 'T%' AND " + typeField + " NOT LIKE 'U%' AND " + typeField
                         + " NOT LIKE 'V%' AND " + typeField + " NOT LIKE 'W%' AND " + typeField + " NOT LIKE 'X%' AND " + typeField
                         + " NOT LIKE 'Y%' AND " + typeField + " NOT LIKE 'Z%' ORDER BY " + typeField);

    String cssFormat = "";
    
    while(rs.next())
    {
      if(cssFormat.equals("line1")) cssFormat = "line2"; else cssFormat = "line1";

      if(typeField.equals("Name"))
        writeBodyLine(viewOnly, out, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), cssFormat, bytesOut);
      else writeBodyLine2(viewOnly, out, rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), cssFormat, bytesOut);
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void setHead(Connection con, Statement stmt, ResultSet rs, PrintWriter out, String userCode, String imagesDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<table id=\"page\" width=100% border=0 cellspacing=\"2\" cellpadding=\"2\">");

    scoutln(out, bytesOut, "<tr><td colspan=20><p><b>By Person Name:</td></tr><tr>");

    int[] count = new int[27];
    searchCount(con, stmt, rs, "Name", userCode, count);

    for(int x=65;x<91;++x)
      writeHeaderLine(out, (char)x, "", count, bytesOut);

    scout(out, bytesOut, "<td align=center><p>");
    if(count[26] > 0)
      scout(out, bytesOut, "<a href=\"javascript:select('-')\">");
    scoutln(out, bytesOut, "Others</a></td></tr>");

    for(int x=65;x<91;++x)
      writeHeaderLine2(out, (char)x, count, bytesOut);

    scoutln(out, bytesOut, "<tr><td colspan=20><p><b>By Company Name:</td></tr><tr>");

    searchCount(con, stmt, rs, "CompanyName", userCode, count);

    for(int x=65;x<91;++x)
      writeHeaderLine(out, (char)x, "2", count, bytesOut);

    scout(out, bytesOut, "<td align=center><p>");
    if(count[26] > 0)
      scout(out, bytesOut, "<a href=\"javascript:select2('-')\">");
    scoutln(out, bytesOut, "Others</a></td></tr>");

    for(int x=65;x<92;++x)
      writeHeaderLine2(out, (char)x, count, bytesOut);
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=27><img src=\"" + imagesDir + "blm.gif\" width=100% height=18></td></tr>");

    scoutln(out, bytesOut, "<tr><td colspan=27><p><a href=\"javascript:add()\">Add New Contact</a>");
    
    scoutln(out, bytesOut, "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:upload()\">Upload Address Book</a>");
    scout(out, bytesOut, "</td></tr>");
    
    scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
    scoutln(out, bytesOut, "<tr><td colspan=27><img src=\"" + imagesDir + "blm2.gif\" width=100% height=3></td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeHeaderLine(PrintWriter out, char which, String type, int[] count, int[] bytesOut) throws Exception
  {
    int x = which - 65;
    scout(out, bytesOut, "<td align=center width=4%><p>");
    
    if(count[x] > 0)
      scout(out, bytesOut, "<a href=\"javascript:select" + type + "('" + which + "')\">" + which + "</a>");
    else scout(out, bytesOut, "" + which);
    
    scoutln(out, bytesOut, "</td>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeHeaderLine2(PrintWriter out, char which, int[] count, int[] bytesOut) throws Exception
  {
    int x = which - 65;
    scout(out, bytesOut, "<td align=center width=4%><p>");
    
    if(count[x] > 0)
      scout(out, bytesOut, "" + count[x]);
    
    scoutln(out, bytesOut, "</td>");
  }
  
  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLine(boolean viewOnly, PrintWriter out, String contactCode, String name, String eMail, String companyName, String customerCode, String supplierCode, String cssFormat, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>");
    if(! viewOnly)
      scoutln(out, bytesOut, "<a href=\"javascript:edit('" + contactCode + "')\">");
    scoutln(out, bytesOut, contactCode + "</a></td><td><p>" + name + "</td>");

    scoutln(out, bytesOut, "<td><p>" + companyName + "</td>");

    scoutln(out, bytesOut, "<td><p><a href=\"javascript:cust('" + customerCode + "')\">" + customerCode + "</a></td><td><p><a href=\"javascript:supp('" + supplierCode + "')\">" + supplierCode + "</a></td>");

    scoutln(out, bytesOut, "<td><p>" + eMail + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void writeBodyLine2(boolean viewOnly, PrintWriter out, String contactCode, String name, String eMail, String companyName, String customerCode, String supplierCode, String cssFormat, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<tr id='" + cssFormat + "'><td nowrap><p>");
    if(! viewOnly)
      scoutln(out, bytesOut, "<a href=\"javascript:edit('" + contactCode + "')\">");
    scoutln(out, bytesOut, contactCode + "</a></td><td><p>" + companyName + "</td>");

    scoutln(out, bytesOut, "<td><p>" + name + "</td>");

//    scoutln(out, bytesOut, "<td><p>" + name + "</td>");
        
    scoutln(out, bytesOut, "<td><p><a href=\"javascript:cust('" + customerCode + "')\">" + customerCode + "</a></td><td><p><a href=\"javascript:supp('" + supplierCode + "')\">" + supplierCode + "</a></td>");
    
    scoutln(out, bytesOut, "<td><p>" + eMail + "</td></tr>");
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void scout(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.print(str);
    bytesOut[0] += str.length();
  }
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

  // -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
  private void searchCount(Connection con, Statement stmt, ResultSet rs, String fld, String userCode, int[] count) throws Exception
  {
    for(int x=0;x<27;++x)
      count[x] = 0;
    
    stmt = con.createStatement();

    rs = stmt.executeQuery("SELECT " + fld + " FROM contacts WHERE Owner = '" + userCode + "'");

    String field;
    
    while(rs.next())
    {
      field = rs.getString(1).toUpperCase();
      
      if(field.length() == 0 || field.charAt(0) < 65 || field.charAt(0) > 92)
        ++count[26];
      else ++count[(field.charAt(0) - 65)];
    }
    
    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
  }

}
