// =======================================================================================================================================================================================================
// System: ZaraStar: Mail: search library
// Module: MailZaraViewSearchLibrary.java
// Author: C.K.Harvey
// Copyright (c) 2000-08 Christopher Harvey. All Rights Reserved.
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

public class MailZaraViewSearchLibrary extends HttpServlet
{
  GeneralUtils generalUtils = new GeneralUtils();
  MessagePage messagePage = new MessagePage();
  ServerUtils serverUtils = new ServerUtils();
  AuthenticationUtils authenticationUtils = new AuthenticationUtils();
  DirectoryUtils directoryUtils = new DirectoryUtils();
  MailUtils mailUtils = new MailUtils();
  
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
  {
    PrintWriter out=null;
    int[] bytesOut = new int[1];  bytesOut[0] = 0;

    String unm="", sid="", uty="", men="", den="", dnm="", bnm="", p1="", p2="", p3="", p4="", p5="", p6="", p7="", p8="", p9="", p10="", p11="",
           p12="", p13="";
                         
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
      p1  = req.getParameter("p1");  // dateFrom
      p2  = req.getParameter("p2");  // dateTo
      p3  = req.getParameter("p3");  // mailFrom
      p4  = req.getParameter("p4");  // mailTo
      p5  = req.getParameter("p5");  // domainFrom
      p6  = req.getParameter("p6");  // domainTo
      p7  = req.getParameter("p7");  // companyCode
      p8  = req.getParameter("p8");  // companyType
      p9  = req.getParameter("p9");  // projectCode
      p10 = req.getParameter("p10"); // textBody
      p11 = req.getParameter("p11"); // subject
      p12 = req.getParameter("p12"); // sentOrReceived
      p13 = req.getParameter("p13"); // userOrAll
      
      if(p1  == null) p1  = "";
      if(p2  == null) p2  = "";
      if(p3  == null) p3  = "";
      if(p4  == null) p4  = "";
      if(p5  == null) p5  = "";
      if(p6  == null) p6  = "";
      if(p7  == null) p7  = "";
      if(p8  == null) p8  = "";
      if(p9  == null) p9  = "";
      if(p10 == null) p10 = "";
      if(p11 == null) p11 = "";
      if(p12 == null) p12 = "";
      if(p13 == null) p13 = "";
      
      doIt(out, req, unm, sid, uty, men, den, dnm, bnm, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, bytesOut);
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

      System.out.println("8008a: " + e);
      messagePage.errorPage(out, req, e, "Communications Error", unm, sid, dnm, bnm, urlBit, men, den, uty, "MailZaraViewSearchLibrary", bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8008, bytesOut[0], 0, "ERR:" + p1);
      if(out != null) out.flush();
    }
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void doIt(PrintWriter out, HttpServletRequest req, String unm, String sid, String uty, String men, String den, String dnm, String bnm,
                    String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8, String p9, String p10, String p11,
                    String p12, String p13, int[] bytesOut) throws Exception
  {
    String imagesDir     = directoryUtils.getSupportDirs('I');
    String defnsDir      = directoryUtils.getSupportDirs('D');
    String localDefnsDir = directoryUtils.getLocalOverrideDir(dnm);

    long startTime = new java.util.Date().getTime();

    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection[] conp = new Connection[1];Connection con = directoryUtils.createConnections(dnm + "_ofsa", conp);
    
    Statement stmt = null;
    ResultSet rs   = null;

    if(! authenticationUtils.verifyAccess(con, stmt, rs, req, 8014, unm, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 13, unm, sid, uty, men, den, dnm, bnm, "MailZaraViewLibrary", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8008, bytesOut[0], 0, "ACC:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    if(! serverUtils.checkSID(unm, sid, uty, dnm, localDefnsDir, defnsDir))
    {
      messagePage.msgScreen(false, out, req, 12, unm, sid, uty, men, den, dnm, bnm, "MailZaraViewLibrary", imagesDir, localDefnsDir, defnsDir, bytesOut);
      serverUtils.etotalBytes(req, unm, dnm, 8008, bytesOut[0], 0, "SID:" + p1);
      if(con != null) con.close();
      if(out != null) out.flush();
      return;
    }

    String dateFrom;
    if(p1.length() == 0)
      dateFrom = "1970-01-01";
    else dateFrom = generalUtils.convertDateToSQLFormat(p1);
    
    String dateTo;
    if(p2.length() == 0)
      dateTo = "2099-12-31";
    else dateTo = generalUtils.convertDateToSQLFormat(p2);
       
    String mailFrom       = generalUtils.stripLeadingAndTrailingSpaces(p3);
    String mailTo         = generalUtils.stripLeadingAndTrailingSpaces(p4);
    String domainFrom     = generalUtils.stripLeadingAndTrailingSpaces(p5);
    String domainTo       = generalUtils.stripLeadingAndTrailingSpaces(p6);
    String companyCode    = generalUtils.stripLeadingAndTrailingSpaces(p7);
    String projectCode    = generalUtils.stripLeadingAndTrailingSpaces(p9);
    String textBody       = generalUtils.stripLeadingAndTrailingSpaces(p10);
    String subject        = generalUtils.stripLeadingAndTrailingSpaces(p11);
    String sentOrReceived = generalUtils.stripLeadingAndTrailingSpaces(p12);
                            
    set(con, stmt, rs, out, req, dateFrom, dateTo, mailFrom, mailTo, domainFrom, domainTo, companyCode, p8, projectCode, textBody, subject,
        sentOrReceived, p13, unm, sid, uty, men, den, dnm, bnm, imagesDir, localDefnsDir, defnsDir, bytesOut);

    serverUtils.totalBytes(con, stmt, rs, req, unm, dnm, 8008, bytesOut[0], 0, (new java.util.Date().getTime() - startTime), p1);
    if(con  != null) con.close();
    if(out != null) out.flush();
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void set(Connection con, Statement stmt, ResultSet rs, PrintWriter out, HttpServletRequest req, String dateFrom, String dateTo,
                   String mailFrom, String mailTo, String domainFrom, String domainTo, String companyCode, String companyType, String projectCode,
                   String textBody, String subject, String sentOrReceived, String userOrAll, String unm, String sid, String uty, String men,
                   String den, String dnm, String bnm, String imagesDir, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    scoutln(out, bytesOut, "<html><head><title>Mail Library</title>");

    scoutln(out, bytesOut, "<script language=\"JavaScript\">");

    scoutln(out, bytesOut, "function fetch(msgCode){");
    scoutln(out, bytesOut, "this.location.href=\"/central/servlet/MailZaraViewFromLibrary?unm=" + unm + "&sid=" + sid + "&uty=" + uty + "&men=" + men + "&den=" + den
                         + "&dnm=" + dnm + "&bnm=" + bnm + "&p1=\"+msgCode;}");

    scoutln(out, bytesOut, "</script><link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"" + directoryUtils.getCssGeneralDirectory(con, stmt, rs, unm, dnm, defnsDir) + "general.css\">");

    int[] hmenuCount = new int[1];
    
    mailUtils.outputPageFrame(con, stmt, rs, out, req, false, "", "", "MailZaraViewLibrary", "8008", unm, sid, uty, men, den, dnm, bnm, "", localDefnsDir, defnsDir, hmenuCount, bytesOut);

    mailUtils.drawTitle(con, stmt, rs, req, out, false, "", "Mail Library", "8008", unm, sid, uty, men, den, dnm, bnm, localDefnsDir, defnsDir, hmenuCount, bytesOut);

    scoutln(out, bytesOut, "<form><table id=\"page\" width=100% border=0>");

    search(out, dateFrom, dateTo, mailFrom, mailTo, domainFrom, domainTo, companyCode, companyType, projectCode, textBody, subject, sentOrReceived,
           userOrAll, unm, dnm, localDefnsDir, defnsDir, bytesOut);

    scoutln(out, bytesOut, "</table></form>");     scoutln(out, bytesOut, authenticationUtils.buildFooter(con, stmt, rs, unm, dnm, bnm, localDefnsDir, defnsDir)); 
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void search(PrintWriter out, String dateFrom, String dateTo, String mailFrom, String mailTo, String domainFrom, String domainTo,
                      String companyCode, String companyType, String projectCode, String textBody, String subject, String sentOrReceived,
                      String userOrAll, String unm, String dnm, String localDefnsDir, String defnsDir, int[] bytesOut) throws Exception
  {
    String userName = directoryUtils.getMySQLUserName();
    String passWord = directoryUtils.getMySQLPassWord();
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + dnm + "_mail?user=" + userName + "&password=" + passWord);
    Statement stmt = con.createStatement();
    
    String where = "Date >= {d '" + dateFrom + " 00:00:00'} AND Date <= {d '" + dateTo + " 23:59:59'}";
    
    if(mailFrom.length() > 0)
      where += " AND AddrFrom = '" + mailFrom + "'";
              
    if(mailTo.length() > 0)
      where += " AND AddrTo = '" + mailTo + "'";
              
    if(domainFrom.length() > 0)
      where += " AND AddrFrom LIKE '" + domainFrom + "%'";
              
    if(domainTo.length() > 0)
      where += " AND AddrTo LIKE '" + domainTo + "%'";
              
    if(companyCode.length() > 0)
      where += " AND CompanyCode = '" + companyCode + "' AND CompanyType = '" + companyType + "'";
              
    if(projectCode.length() > 0)
      where += " AND ProjectCode = '" + projectCode + "'";
              
    if(subject.length() > 0)
      where += " AND Subject LIKE '%" + subject + "%'";

    if(sentOrReceived.length() > 0)
    {
      switch(sentOrReceived.charAt(0))
      {
        case 'S' : where += " AND SentOrReceived = 'S'"; break;
        case 'R' : where += " AND SentOrReceived = 'R'"; break;
      }
    }

    ResultSet rs = stmt.executeQuery("SELECT MsgCode, SentOrReceived, AddrFrom, AddrTo, Date, Subject, Attachments, Text, Linkage, CompanyCode, "
                                   + "ProjectCode FROM mail WHERE " + where + " ORDER BY Date");

    String msgCode, date, addrFrom, addrTo, attachments, text, linkage, cssFormat="";
    boolean first = true;

    while(rs.next())
    {    
      msgCode        = rs.getString(1);
      sentOrReceived = rs.getString(2);
      addrFrom       = rs.getString(3);
      addrTo         = rs.getString(4);
      date           = rs.getString(5);
      subject        = rs.getString(6);
      attachments    = rs.getString(7);
      text           = rs.getString(8);
      linkage        = rs.getString(9);
      companyCode    = rs.getString(10);
      projectCode    = rs.getString(11);

      if(sentOrReceived.equals("S"))
        sentOrReceived = "Sent";
      else
      if(sentOrReceived.equals("R"))
        sentOrReceived = "Received";
      else sentOrReceived = "?"; // just-in-case
      
      if(first)
      {
        scoutln(out, bytesOut, "<tr><td>&nbsp;</td></tr>");
        scoutln(out, bytesOut, "<tr id=\"pageColumn\"><td></td><td><p>Message Code</td>");
        scoutln(out, bytesOut, "<td><p>Sent/Received</td>");
        scoutln(out, bytesOut, "<td><p>From</td>");
        scoutln(out, bytesOut, "<td><p>To</td>");
        scoutln(out, bytesOut, "<td><p>Date</td>");
        scoutln(out, bytesOut, "<td><p>Subject</td>");
        scoutln(out, bytesOut, "<td><p>CompanyCode</td>");
        scoutln(out, bytesOut, "<td><p>ProjectCode</td></tr>");
        first = false;
      }

      if(cssFormat.equals("line1"))
        cssFormat = "line2";
      else cssFormat = "line1";

      scoutln(out, bytesOut, "<tr id=\"" + cssFormat + "\"><td></td><td><p>" + msgCode + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + sentOrReceived + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + addrFrom + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + addrTo + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + generalUtils.convertFromYYYYMMDD(date) + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + subject + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + companyCode + "</td>");
      scoutln(out, bytesOut, "<td nowrap><p>" + projectCode + "</td>");
      scoutln(out, bytesOut, "<td><p><a href=\"javascript:fetch('" + msgCode + "')\">Read</a></td></tr>");
    }

    if(rs   != null) rs.close();
    if(stmt != null) stmt.close();
    if(con  != null) con.close();  
  }

  // -------------------------------------------------------------------------------------------------------------------------------------------------
  private void scoutln(PrintWriter out, int bytesOut[], String str) throws Exception
  {      
    out.println(str);
    bytesOut[0] += (str.length() + 2);    
  }

}
